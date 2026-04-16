package es.ual.dra.autodiagnostico.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.io.File;

import es.ual.dra.autodiagnostico.model.entitity.Product;
import es.ual.dra.autodiagnostico.model.entitity.Vehicle;
import es.ual.dra.autodiagnostico.repository.VehicleRepository;

import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Servicio encargado de realizar el scraping de datos de vehículos y sus
 * productos asociados.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UltimateSpecsVehicleScraperService {

        private final VehicleRepository vehicleRepository;

        /**
         * Realiza el scraping de una URL dada y guarda el vehículo y sus productos en
         * la base de datos.
         * 
         * @param url La URL objetivo para el scraping.
         * @return El vehículo persistido con sus productos.
         * @throws IOException Si ocurre un error de conexión.
         */
        @Transactional
        public void scrapeAndSave() throws IOException {
                final String url = "https://www.ultimatespecs.com/es";
                System.out.println(">>> SCRAPER STARTED <<<");
                log.info("Iniciando scraping de la URL: {}", url);

                // 1. Configuración de Jsoup: Conexión y parseo
                // Se utiliza un User-Agent para evitar bloqueos por parte del servidor.
                Document doc = Jsoup.connect(url)
                                .userAgent(
                                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                .timeout(10000)
                                .get();

                // 2. Lógica de Extracción: Captura de datos generales del vehículo
                // NOTA: Los selectores CSS son representativos y deben ajustarse al DOM real de
                // la web objetivo.
                Elements brands = doc.select(".home_brands .col-md-2.col-sm-3.col-xs-4.col-4");

                File outputDir = new File("logos");
                outputDir.mkdirs();

                for (Element brand : brands) {

                        String brandName = brand.select(".home_brand").text();

                        Element img = brand.selectFirst(".home_brand_logo img");

                        String spriteUrl = "";
                        int x = 0;
                        int y = 0;

                        if (img != null) {

                                String style = img.attr("style");

                                int urlStart = style.indexOf("url('");
                                int urlEnd = style.indexOf("')", urlStart);

                                if (urlStart != -1 && urlEnd != -1) {
                                        spriteUrl = style.substring(urlStart + 5, urlEnd);
                                }

                                String[] parts = style.split(" ");

                                for (String p : parts) {
                                        if (p.endsWith("px")) {
                                                int val = Math.abs(
                                                                Integer.parseInt(p.replace("px", "").replace(";", "")));

                                                if (x == 0)
                                                        x = val;
                                                else
                                                        y = val;
                                        }
                                }
                        }

                        if (!spriteUrl.isEmpty()) {

                                BufferedImage sprite = ImageIO.read(
                                                new URL("https://www.ultimatespecs.com" + spriteUrl));

                                BufferedImage logo = sprite.getSubimage(x, y, 60, 60);

                                File out = new File(outputDir, brandName.replaceAll("[^a-zA-Z0-9]", "_") + ".png");
                                ImageIO.write(logo, "png", out);
                        }

                        System.out.println("Brand: " + brandName);
                }
        }
}
