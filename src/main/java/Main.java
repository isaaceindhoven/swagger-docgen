import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Language;
import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import io.github.swagger2markup.markup.builder.MarkupLanguage;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Created by cas.eliens on 31-Aug-16.
 */
public class Main {
        public static void main( String[] args ) throws MalformedURLException {

            LocalDateTime dt = LocalDateTime.now();

            // Versioned names for debugging
            String year = String.valueOf(dt.getYear());
            String month = String.valueOf(dt.getMonthValue());
            String day = String.valueOf(dt.getDayOfMonth());
            String hour = String.valueOf(dt.getHour());
            String minute = String.valueOf(dt.getMinute());
            String second = String.valueOf(dt.getSecond());

            String hash = String.format("%4s_%2s_%2s-%2s_%2s_%2s", year, month, day, hour, minute, second).replace(' ', '0');


            //URL swaggerFile = new URL("http://petstore.swagger.io/v2/swagger.json");
            Path swaggerFile = Paths.get("spec.yaml");
            Path outputFile = Paths.get("build/asciidoc/api_" + hash);

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
        }
}
