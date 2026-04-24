package es.ual.dra.autodiagnostico.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
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
import java.util.LinkedHashMap;
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

                // int debugCount = 0;

                boolean foundDodge = false;

                for (Element brand : brands) {

                        Map<String, Object> brandData = new HashMap<>();

                        String brandName = brand.select(".home_brand").text();

                        // 2. Logic to skip until Dodge is hit
                        if (!foundDodge) {
                                if (brandName.equalsIgnoreCase("Volkswagen")) {
                                        foundDodge = true;
                                } else {
                                        continue;
                                }
                        }

                        // if (!brandName.toLowerCase().trim().equals("seat")) {
                        // continue;
                        // }
                        Element img = brand.selectFirst(".home_brand_logo img");

                        brandData.put("brandName", brandName);

                        String brandFile = String.format("ultimatespecs-%s.json", brandName);
                        Path outputPath = Paths.get("scraper-output", brandFile);
                        Files.createDirectories(outputPath.getParent());

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

                        Files.writeString(outputPath, toJson(brandData));

                        log.info("Preview JSON generated at {}", outputPath.toAbsolutePath());

                        // Introduce a wait with a random between 1-10 seconds to avoid Cloudflare 520
                        // error:
                        CloudflareDelay.waitRandomTime();
                }
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
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                .referrer("https://www.ultimatespecs.com/es")
                                .timeout(10000)
                                .get();
                // Implementar forma de acceder a href="/es/car-specs/Abarth" para expandir la
                // lista de modelos, ya que actualmente solo se muestran 5 modelos por marca y
                // hay marcas con más modelos (ejemplo: Abarth)
                // modelBlocks son todos los dconnectiv de las "filas"
                Elements modelRow = doc.select("div.home_models_line");

                // Debug: print model blocks count and total versions count
                int versionsCount = 0;
                for (Element model : modelRow) {
                        // los children de modelBlock son los modelos finales
                        versionsCount += model.childrenSize();
                }

                System.out.println("Found " + versionsCount + " car model versions for brand " + brandUrl);

                File outputDir = new File("models");
                outputDir.mkdirs();

                for (Element model : modelRow) {

                        for (Element subModel : model.children()) {

                                Element modelLink = subModel.selectFirst("a[href]");
                                if (modelLink == null) {
                                        modelLink = subModel.selectFirst("a[href]");
                                }

                                if (modelLink == null) {
                                        System.out.println("SKIPPING MODEL BLOCK MODEL LINK!");
                                        continue;
                                }

                                String modelUrl = modelLink.absUrl("href");
                                if (modelUrl == null || modelUrl.isBlank()) {
                                        System.out.println("SKIPPING MODEL BLOCK MODEL URL!");
                                        continue;
                                }

                                System.out.println("URL: " + modelUrl);

                                Map<String, Object> modelData = new HashMap<>();
                                modelData.put("url", modelUrl);

                                Element img = subModel.selectFirst("img");
                                Element title = subModel.selectFirst("h2");

                                Document modelDoc = Jsoup.connect(modelUrl)
                                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                                .referrer(brandUrl)
                                                .timeout(10000)
                                                .get();

                                List<Map<String, Object>> versionsList = new ArrayList<>();

                                Elements finalModels = modelDoc.select("div.home_models_line");
                                Element versionDiv = modelDoc.selectFirst("#versions");
                                if (!finalModels.isEmpty() && versionDiv == null) {
                                        for (Element finalModel : finalModels) {
                                                Elements finalModelLinks = finalModel.children().select("a[href]");

                                                for (Element finalModelLink : finalModelLinks) {
                                                        if (finalModelLink == null) {
                                                                finalModelLink = finalModel.selectFirst("a[href]");
                                                        }

                                                        if (finalModelLink == null) {
                                                                continue;
                                                        }

                                                        String versionUrl = finalModelLink.absUrl("href");
                                                        if (versionUrl == null || versionUrl.isBlank()) {
                                                                continue;
                                                        }

                                                        String actualFinalModeName = finalModel.children().select("h2")
                                                                        .text();

                                                        System.out.println("Found version href: " + versionUrl);
                                                        versionsList.add(scrapeVersion(versionUrl,
                                                                        actualFinalModeName));
                                                }

                                        }
                                } else {

                                        // Elements versionLinks = modelDoc.select(
                                        // ".versions_div a[href], .table_versions a[href], #versions a[href]");

                                        // if (!versionLinks.isEmpty()) {
                                        // for (Element versionLink : versionLinks) {
                                        // String versionUrl = versionLink.absUrl("href");
                                        // if (versionUrl == null || versionUrl.isBlank()) {
                                        // continue;
                                        // }

                                        // System.out.println("Found version href: " + versionUrl);
                                        // versionsList.add(scrapeVersion(versionUrl));
                                        // }
                                        // } else {
                                        // System.out.println(
                                        // "NO VERSION BLOCKS FOUND, SCRAPING MODEL PAGE AS FALLBACK");
                                        // versionsList.add(scrapeVersion(modelUrl));
                                        // }
                                        String actualFinalModelName = modelDoc.selectFirst(".page_title_text")
                                                        .selectFirst("h1").text();
                                        versionsList.add(scrapeVersion(modelUrl, actualFinalModelName));
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
                                // Introduce a wait with a random between 1-10 seconds to avoid Cloudflare 520
                                // error:
                                CloudflareDelay.waitRandomTime();
                        }

                }

                System.out.println("SCRAPED MODELS FOR BRAND EXECUTION FINISHED");
                return modelsData;
        }

        private Map<String, Object> scrapeVersion(String urlFinalModel, String actualFinalModeName) {
                Map<String, Object> versionData = new HashMap<>();
                versionData.put("url", urlFinalModel);
                versionData.put("actualFinalModelName", actualFinalModeName);
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
                                        specContainer = col12.selectFirst(
                                                        "div[style*=margin-left:10px][style*=margin-top:5px]");
                                        if (specContainer == null) {
                                                specContainer = col12.selectFirst(
                                                                "div[style*=margin-left][style*=margin-top]");
                                        }
                                        if (specContainer == null) {
                                                specContainer = col12.selectFirst("div");
                                        }
                                }

                                if (specContainer != null) {
                                        Map<String, String> specsMap = new HashMap<>();
                                        Elements titleElements = specContainer.select("b");

                                        for (Element titleElement : titleElements) {
                                                String title = titleElement.text().trim();
                                                if (title.isEmpty()) {
                                                        continue;
                                                }

                                                StringBuilder valueBuilder = new StringBuilder();
                                                for (Node sibling = titleElement
                                                                .nextSibling(); sibling != null; sibling = sibling
                                                                                .nextSibling()) {
                                                        if (sibling instanceof Element siblingElement) {
                                                                if ("br".equalsIgnoreCase(siblingElement.tagName())) {
                                                                        break;
                                                                }

                                                                if ("b".equalsIgnoreCase(siblingElement.tagName())) {
                                                                        break;
                                                                }

                                                                if ("i".equalsIgnoreCase(siblingElement.tagName())
                                                                                && (siblingElement.hasClass(
                                                                                                "fa-dot-circle")
                                                                                                || siblingElement
                                                                                                                .hasClass("fa-dot-circle-o"))) {
                                                                        break;
                                                                }

                                                                String text = siblingElement.text().trim();
                                                                if (!text.isEmpty()) {
                                                                        if (!valueBuilder.isEmpty()) {
                                                                                valueBuilder.append(' ');
                                                                        }
                                                                        valueBuilder.append(text);
                                                                }
                                                                continue;
                                                        }

                                                        if (sibling instanceof TextNode textNode) {
                                                                String text = textNode.text().trim();
                                                                if (!text.isEmpty()) {
                                                                        if (!valueBuilder.isEmpty()) {
                                                                                valueBuilder.append(' ');
                                                                        }
                                                                        valueBuilder.append(text);
                                                                }
                                                        }
                                                }

                                                String value = valueBuilder.toString().replaceAll("\\s+", " ").trim();
                                                if (!value.isEmpty()) {
                                                        System.out.println(title + " -> " + value);
                                                        specsMap.put(title, value);
                                                }
                                        }

                                        versionData.put("specifications", specsMap);
                                }
                        }

                        scrapeTableVersions(doc, versionData, urlFinalModel);

                } catch (IOException e) {
                        log.error("Error scraping version {}: {}", urlFinalModel, e.getMessage());
                }
                return versionData;
        }

        private void scrapeTableVersions(Document doc, Map<String, Object> versionData, String urlFinalModel) {
                // If the engine sections are hidden behind the fuel-sort button, find that
                // button's parent anchor in the static HTML and follow its href.
                Document activeDoc = doc;
                Element fuelButtonImg = doc.selectFirst("img[src*='fuelw.png']");
                if (fuelButtonImg != null) {
                        Element parentAnchor = fuelButtonImg.closest("a[href]");
                        if (parentAnchor != null) {
                                String fuelUrl = parentAnchor.absUrl("href");
                                if (!fuelUrl.isBlank()) {
                                        try {
                                                System.out.println("Following fuel-sort button href: " + fuelUrl);
                                                activeDoc = Jsoup.connect(fuelUrl)
                                                                .userAgent(
                                                                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                                                                .referrer(urlFinalModel)
                                                                .timeout(10000)
                                                                .get();
                                        } catch (IOException e) {
                                                log.error("Error fetching fuel-sort URL {}: {}", fuelUrl,
                                                                e.getMessage());
                                        }
                                }
                        }
                }

                Element versionssDiv = activeDoc.selectFirst("#versions");
                if (versionssDiv == null) {
                        log.warn("No #versions div found for URL: {}", urlFinalModel);
                        return;
                }

                List<Element> engine_divs = new ArrayList<>();
                engine_divs.add(versionssDiv.selectFirst("#electric_engines"));
                engine_divs.add(versionssDiv.selectFirst("#pluginhybrid_engines"));
                engine_divs.add(versionssDiv.selectFirst("#petrol_engines"));
                engine_divs.add(versionssDiv.selectFirst("#diesel_engines"));
                engine_divs.add(versionssDiv.selectFirst("#other_engines"));

                List<Map<String, String>> tableData = new ArrayList<>();

                System.out.println("url: " + urlFinalModel);

                for (Element engineVersion : engine_divs) {
                        if (engineVersion == null) {
                                continue;
                        }
                        Element table_version = engineVersion.selectFirst(".table_versions");
                        if (table_version == null) {
                                continue;
                        }

                        System.out.println("table_version: " + table_version);
                        Elements rows = table_version.select("tr");

                        if (!rows.isEmpty()) {
                                Elements headerCells = rows.get(0).select("th, td");
                                List<String> headers = new ArrayList<>();
                                for (Element headerCell : headerCells) {
                                        String headerText = headerCell.text().trim();
                                        headers.add(headerText.isEmpty()
                                                        ? "column" + (headers.size() + 1)
                                                        : headerText);
                                }

                                for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
                                        Elements cells = rows.get(rowIndex).select("td, th");
                                        if (cells.isEmpty()) {
                                                continue;
                                        }

                                        Map<String, String> rowData = new HashMap<>();
                                        for (int i = 0; i < cells.size(); i++) {
                                                String key = i < headers.size() ? headers.get(i)
                                                                : "column" + (i + 1);
                                                String value = cells.get(i).text().trim();
                                                System.out.println(key + " -> " + value);
                                                rowData.put(key, value);
                                        }

                                        if (!rowData.isEmpty()) {
                                                tableData.add(rowData);
                                        }
                                }
                        }
                }

                System.out.println("tableData: " + tableData);
                versionData.put("table_versions", tableData);
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
                                                        .append('"').append(escapeJson(String.valueOf(entry.getKey())))
                                                        .append('"')
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
