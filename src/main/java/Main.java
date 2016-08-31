import io.github.swagger2markup.Swagger2MarkupConverter;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by cas.eliens on 31-Aug-16.
 */
public class Main {
        public static void main( String[] args ) throws MalformedURLException {
            System.out.println( "Hello World!" );

            URL location = Main.class.getProtectionDomain().getCodeSource().getLocation();
            System.out.println("I'm running from " + location.getFile());

            URL remoteSwaggerFile = new URL("http://petstore.swagger.io/v2/swagger.json");
            Path outputDirectory = Paths.get("build/asciidoc");

            Swagger2MarkupConverter.from(remoteSwaggerFile)
                    .build()
                    .toFolder(outputDirectory);
        }
}
