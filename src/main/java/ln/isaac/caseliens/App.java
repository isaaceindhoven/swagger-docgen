package ln.isaac.caseliens;

import io.github.swagger2markup.Swagger2MarkupConverter;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        Path localSwaggerFile = Paths.get("../../spec.yaml");
        Path outputDirectory = Paths.get("../../asciidoc");

        Swagger2MarkupConverter.from(localSwaggerFile)
                .build()
                .toFolder(outputDirectory);
    }
}
