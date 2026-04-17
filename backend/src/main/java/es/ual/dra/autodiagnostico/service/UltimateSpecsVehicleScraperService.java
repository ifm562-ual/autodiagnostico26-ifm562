package main.java.es.ual.dra.autodiagnostico.service;

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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Servicio encargado de realizar el scraping de datos de vehículos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UltimateSpecsVehicleScraperService {

    @Transactional
    public void scrapeAndSave() throws IOException {
        final String url = "https://www.ultimatespecs.com/es";

        System.out.println(">>> SCRAPER STARTED <<<");
        log.info("Iniciando scraping de la URL: {}", url);

        Document doc = Jsoup.connect(url)
                .userAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(10000)
                .get();

        Elements brands = doc.select(".home_brands a[href]");

        File outputDir = new File("logos");
        outputDir.mkdirs();

        List<Map<String, Object>> allBrandsData = new ArrayList<>();

        for (Element brand : brands) {
            Map<String, Object> brandData = new HashMap<>();

            String brandName = brand.select(".home_brand").text();
            Element img = brand.selectFirst(".home_brand_logo img");

            brandData.put("brandName", brandName);

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
                        int val = Math.abs(Integer.parseInt(p.replace("px", "").replace(";", "")));

                        if (x == 0) {
                            x = val;
                        } else {
                            y = val;
                        }
                    }
                }
            }

            if (!spriteUrl.isEmpty()) {
                BufferedImage sprite = ImageIO.read(
                        URI.create("https://www.ultimatespecs.com" + spriteUrl).toURL());

                BufferedImage logo = sprite.getSubimage(x, y, 60, 60);

                File out = new File(outputDir,
                        brandName.replaceAll("[^a-zA-Z0-9]", "_") + ".png");

                ImageIO.write(logo, "png", out);
            }

            System.out.println("Brand: " + brandName);

            List<Map<String, Object>> modelsData = scrapeModelsForBrand(brand);
            brandData.put("models", modelsData);
            allBrandsData.add(brandData);

            log.info("ModelsData:\n{}", formatForDebug(modelsData));

            // Debug mode: only first brand
            break;
        }

        Path outputPath = Paths.get("scraper-output", "ultimatespecs-first-elements.json");
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, toJson(allBrandsData));
        log.info("Preview JSON generated at {}", outputPath.toAbsolutePath());
    }

    /**
     * SECOND LEVEL SCRAPER:
     * Extracts models from each brand page
     */
    private List<Map<String, Object>> scrapeModelsForBrand(Element brandElement) throws IOException {
        List<Map<String, Object>> modelsData = new ArrayList<>();
        String brandUrl = brandElement.absUrl("href");
        if (brandUrl == null || brandUrl.isBlank()) {
            System.out.println("NO BRAND URL");
            return modelsData;
        }

        log.info("Navigating to brand page: {}", brandUrl);

        Document doc = Jsoup.connect(brandUrl)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get();

        Elements modelBlocks = doc.select("div.home_models_line.gene");
        System.out.println("Found " + modelBlocks.size() + " model blocks for brand " + brandUrl);

        File outputDir = new File("models");
        outputDir.mkdirs();

        for (Element modelBlock : modelBlocks) {
            Element modelLink = modelBlock.children().selectFirst("a[href]");
            if (modelLink == null) {
                modelLink = modelBlock.selectFirst("a[href]");
            }

            if (modelLink == null) {
                continue;
            }

            String modelUrl = modelLink.absUrl("href");
            if (modelUrl == null || modelUrl.isBlank()) {
                continue;
            }

            Map<String, Object> modelData = new HashMap<>();
            modelData.put("url", modelUrl);

            Element img = modelBlock.selectFirst("img");
            Element title = modelBlock.selectFirst("h2");

            Document modelDoc = Jsoup.connect(modelUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            List<Map<String, Object>> versionsList = new ArrayList<>();

            Elements versionBlocks = modelDoc.select("div.home_models_line.gene");
            if (!versionBlocks.isEmpty()) {
                for (Element versionBlock : versionBlocks) {
                    Element versionLink = versionBlock.children().selectFirst("a[href]");
                    if (versionLink == null) {
                        versionLink = versionBlock.selectFirst("a[href]");
                    }

                    if (versionLink == null) {
                        continue;
                    }

                    String versionUrl = versionLink.absUrl("href");
                    if (versionUrl == null || versionUrl.isBlank()) {
                        continue;
                    }

                    System.out.println("Found version href: " + versionUrl);
                    versionsList.add(scrapeVersion(versionUrl));
                }
            } else {
                Elements versionLinks = modelDoc.select("#versions a[href]");
                if (!versionLinks.isEmpty()) {
                    for (Element versionLink : versionLinks) {
                        String versionUrl = versionLink.absUrl("href");
                        if (versionUrl == null || versionUrl.isBlank()) {
                            continue;
                        }

                        System.out.println("Found version href: " + versionUrl);
                        versionsList.add(scrapeVersion(versionUrl));
                    }
                } else {
                    System.out.println("NO VERSION BLOCKS FOUND, SCRAPING MODEL PAGE AS FALLBACK");
                    versionsList.add(scrapeVersion(modelUrl));
                }
            }

            modelData.put("versions", versionsList);

            String modelName = title != null ? title.text() : "unknown";
            modelData.put("modelName", modelName);

            String imageUrl = "";
            if (img != null) {
                imageUrl = img.attr("abs:src");
                if (imageUrl.isEmpty()) {
                    imageUrl = "https:" + img.attr("src");
                }
            }
            modelData.put("imageUrl", imageUrl);

            System.out.println(modelName + " -> " + imageUrl);

            if (!imageUrl.isEmpty()) {
                downloadModelImage(imageUrl, brandUrl, modelName);
            }
            modelsData.add(modelData);
        }

        System.out.println("SCRAPED MODELS FOR BRAND EXECUTION FINISHED");
        return modelsData;
    }

    private Map<String, Object> scrapeVersion(String urlFinalModel) {
        Map<String, Object> versionData = new HashMap<>();
        versionData.put("url", urlFinalModel);
        try {
            Document doc = Jsoup.connect(urlFinalModel)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Element img = doc.selectFirst(".left_column_top_model_image_div");

            Element resumo_ficha = doc.selectFirst(".resumo_ficha");

            if (resumo_ficha != null) {
                Element col12 = resumo_ficha.selectFirst(".col-12");
                Element specContainer = null;
                if (col12 != null) {
                    specContainer = col12.selectFirst("div[style*=margin-left:10px][style*=margin-top:5px]");
                    if (specContainer == null) {
                        specContainer = col12.selectFirst("div[style*=margin-left][style*=margin-top]");
                    }
                    if (specContainer == null) {
                        specContainer = col12.selectFirst("div");
                    }
                }

                if (specContainer != null) {
                    Elements specsElements = specContainer.select(".fa-dot-circle");
                    Map<String, String> specsMap = new HashMap<>();
                    for (Element specification : specsElements) {
                        Element title = specification.selectFirst("b");
                        Element value = specification.selectFirst("span");
                        if (title != null && value != null) {
                            System.out.println(title.text() + " -> " + value.text());
                            specsMap.put(title.text(), value.text());
                        }
                    }
                    versionData.put("specifications", specsMap);
                }
            }

            Element table_versions = doc.selectFirst(".table_versions");

            if (table_versions != null) {
                Element thead = table_versions.selectFirst("thead");
                Elements headers = thead != null ? thead.select("th") : new Elements();

                Element tbody = table_versions.selectFirst("tbody");
                Elements rows = tbody != null ? tbody.select("tr") : new Elements();

                List<Map<String, String>> tableData = new ArrayList<>();
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    Map<String, String> rowData = new HashMap<>();
                    for (int i = 0; i < headers.size() && i < cells.size(); i++) {
                        System.out.println(headers.get(i).text() + " -> " + cells.get(i).text());
                        rowData.put(headers.get(i).text(), cells.get(i).text());
                    }
                    tableData.add(rowData);
                }
                versionData.put("table_versions", tableData);
            }

        } catch (IOException e) {
            log.error("Error scraping version {}: {}", urlFinalModel, e.getMessage());
        }
        return versionData;
    }

    private void downloadModelImage(String imageUrl, String brandUrl, String modelName) {
        try {
            BufferedImage image = ImageIO.read(
                    URI.create(imageUrl.startsWith("//") ? "https:" + imageUrl : imageUrl).toURL());

            File dir = new File("models/" + sanitize(brandUrl));
            dir.mkdirs();

            File out = new File(dir, sanitize(modelName) + ".png");

            ImageIO.write(image, "png", out);

        } catch (IOException e) {
            log.error("Error downloading model image {}: {}", imageUrl, e.getMessage());
        }
    }

    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private String formatForDebug(Object value) {
        StringBuilder builder = new StringBuilder();
        appendFormatted(builder, value, 0);
        return builder.toString();
    }

    private void appendFormatted(StringBuilder builder, Object value, int depth) {
        if (value == null) {
            builder.append("<null>");
            return;
        }

        if (value instanceof Map<?, ?> mapValue) {
            builder.append("{\n");
            int index = 0;
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                builder.append(indent(depth + 1))
                        .append(String.valueOf(entry.getKey()))
                        .append(": ");
                appendFormatted(builder, entry.getValue(), depth + 1);
                if (index < mapValue.size() - 1) {
                    builder.append(',');
                }
                builder.append('\n');
                index++;
            }
            builder.append(indent(depth)).append('}');
            return;
        }

        if (value instanceof List<?> listValue) {
            builder.append("[\n");
            for (int i = 0; i < listValue.size(); i++) {
                builder.append(indent(depth + 1));
                appendFormatted(builder, listValue.get(i), depth + 1);
                if (i < listValue.size() - 1) {
                    builder.append(',');
                }
                builder.append('\n');
            }
            builder.append(indent(depth)).append(']');
            return;
        }

        if (value instanceof String stringValue) {
            builder.append('"').append(stringValue).append('"');
            return;
        }

        builder.append(String.valueOf(value));
    }

    private String indent(int depth) {
        return "  ".repeat(depth);
    }

    private String toJson(Object value) {
        StringBuilder builder = new StringBuilder();
        appendJson(builder, value, 0);
        return builder.toString();
    }

    private void appendJson(StringBuilder builder, Object value, int depth) {
        if (value == null) {
            builder.append("null");
            return;
        }

        if (value instanceof Map<?, ?> mapValue) {
            builder.append("{");
            if (!mapValue.isEmpty()) {
                builder.append('\n');
                int index = 0;
                for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                    builder.append(indent(depth + 1))
                            .append('"').append(escapeJson(String.valueOf(entry.getKey()))).append('"')
                            .append(": ");
                    appendJson(builder, entry.getValue(), depth + 1);
                    if (index < mapValue.size() - 1) {
                        builder.append(',');
                    }
                    builder.append('\n');
                    index++;
                }
                builder.append(indent(depth));
            }
            builder.append('}');
            return;
        }

        if (value instanceof List<?> listValue) {
            builder.append("[");
            if (!listValue.isEmpty()) {
                builder.append('\n');
                for (int i = 0; i < listValue.size(); i++) {
                    builder.append(indent(depth + 1));
                    appendJson(builder, listValue.get(i), depth + 1);
                    if (i < listValue.size() - 1) {
                        builder.append(',');
                    }
                    builder.append('\n');
                }
                builder.append(indent(depth));
            }
            builder.append(']');
            return;
        }

        if (value instanceof String stringValue) {
            builder.append('"').append(escapeJson(stringValue)).append('"');
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
            return;
        }

        builder.append('"').append(escapeJson(String.valueOf(value))).append('"');
    }

    private String escapeJson(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
