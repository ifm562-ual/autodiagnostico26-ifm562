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
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import es.ual.dra.autodiagnostico.repository.VehicleRepository;

/**
 * Servicio encargado de realizar el scraping de datos de vehículos y sus
 * productos asociados.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UltimateSpecsVehicleScraperService {

        private final VehicleRepository vehicleRepository;

        @Transactional
        public void scrapeAndSave() throws IOException {

                final String url = "https://www.ultimatespecs.com/es";

                System.out.println(">>> SCRAPER STARTED <<<");
                log.info("Iniciando scraping de la URL: {}", url);

                Document doc = Jsoup.connect(url)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                .timeout(10000)
                                .get();

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

                                File out = new File(outputDir,
                                                brandName.replaceAll("[^a-zA-Z0-9]", "_") + ".png");

                                ImageIO.write(logo, "png", out);
                        }

                        System.out.println("Brand: " + brandName);

                        // ===========================
                        // NEW: scrape models per brand
                        // ===========================
                        scrapeModelsForBrand(brand);
                }
        }

        /**
         * SECOND LEVEL SCRAPER:
         * Extracts models from each brand page
         */
        private void scrapeModelsForBrand(Element brandElement) throws IOException {

                Element link = brandElement.parent().selectFirst("a[href]");
                if (link == null) {
                        System.out.println("NO LINK");
                        return;
                }

                String brandUrl = link.absUrl("href");

                if (brandUrl == null || brandUrl.isEmpty()) {
                        System.out.println("NO BRAND URL");
                        return;
                }

                log.info("Navigating to brand page: {}", brandUrl);

                Document doc = Jsoup.connect(brandUrl)
                                .userAgent("Mozilla/5.0")
                                .timeout(10000)
                                .get();

                Elements models = doc.select(".home_models_line");

                for (Element model : models) {

                        Element img = model.selectFirst(".model_image img");
                        Element title = model.selectFirst(".home_models_over h2");

                        String modelName = (title != null) ? title.text() : "unknown";
                        String imageUrl = "";

                        if (img != null) {
                                imageUrl = img.absUrl("src");
                        }

                        System.out.println("   Model: " + modelName + " -> " + imageUrl);

                        // Optional: download model image
                        if (!imageUrl.isEmpty()) {
                                downloadModelImage(imageUrl, brandUrl, modelName);
                        }
                }
        }

        /**
         * Downloads model image
         */
        private void downloadModelImage(String imageUrl, String brandUrl, String modelName) {

                try {
                        BufferedImage image = ImageIO.read(new URL(imageUrl));

                        File dir = new File("models/" + sanitize(brandUrl));
                        dir.mkdirs();

                        File out = new File(dir, sanitize(modelName) + ".png");

                        ImageIO.write(image, "png", out);

                } catch (IOException e) {
                        log.error("Error downloading model image {}: {}", imageUrl, e.getMessage());
                }
        }

        /**
         * Safe filesystem naming
         */
        private String sanitize(String input) {
                return input.replaceAll("[^a-zA-Z0-9]", "_");
        }
}