import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Language;
import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import io.github.swagger2markup.markup.builder.MarkupLanguage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Created by cas.eliens on 31-Aug-16.
 */
public class Main {
    public static void main(String[] args) {
        try {
            File folder = new File("work/temp");
            emptyFolder(folder);
            //URL swaggerFile = new URL("http://petstore.swagger.io/v2/swagger.json");
            Path swaggerFile = Paths.get("work/input/spec.yaml");
            Path outputFile = Paths.get("work/temp/api_" + getDateString());

            Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                    .withMarkupLanguage(MarkupLanguage.ASCIIDOC)
                    .withOutputLanguage(Language.EN)
                    .withPathsGroupedBy(GroupBy.TAGS)
                    .withGeneratedExamples()
                    .withInterDocumentCrossReferences()
                    .build();

            Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(swaggerFile)
                    .withConfig(config)
                    .build();


            converter.toFile(outputFile);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Generate a string representing the current date and time
     *
     * @return String, formatted yyyy_MM_dd-HH_mm_ss
     */
    private static String getDateString() {
        LocalDateTime dt = LocalDateTime.now();
        String year = String.valueOf(dt.getYear());
        String month = String.valueOf(dt.getMonthValue());
        String day = String.valueOf(dt.getDayOfMonth());
        String hour = String.valueOf(dt.getHour());
        String minute = String.valueOf(dt.getMinute());
        String second = String.valueOf(dt.getSecond());

        return String.format("%4s_%2s_%2s-%2s_%2s_%2s", year, month, day, hour, minute, second).replace(' ', '0');
    }

    public static void emptyFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                f.delete();
            }
        }
    }
}
