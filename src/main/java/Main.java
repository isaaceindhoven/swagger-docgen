import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Language;
import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import io.github.swagger2markup.markup.builder.MarkupLanguage;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.OptionsBuilder.options;

public class Main {
    public static void main(String[] args) {
        try {
            // Setup CLI argument parsing
            Options options = new Options();
            options.addOption("i", "input", true, "Input file");
            options.addOption("o", "output", true, "Output file");

            options.addOption("s", "style", true, "Asciidoctor PDF style");
            options.addOption("d", "styledir", true, "Asciidoctor style directory");
            options.addOption("p", "imagesdir", true, "Asciidoctor images directory");
            options.addOption("f", "fontsdir", true, "Asciidoctor fonts directory");
            options.addOption("t", "toc", false, "Include table of contents");

            options.addOption("g", "groupbytags", false, "Group paths by tags, instead of following the same order as defined by in the spec");
            options.addOption("e", "noexamples", false, "Skip generating example requests.");


            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            String style = (cmd.hasOption('s')) ? cmd.getOptionValue('s') : "default";
            String styleDir = (cmd.hasOption('d')) ? cmd.getOptionValue('d') : "styles";
            String imagesDir = (cmd.hasOption('p')) ? cmd.getOptionValue('p') : "styles/img";
            String fontsDir = (cmd.hasOption('f')) ? cmd.getOptionValue('f') : "styles/fonts";

            String input = (cmd.hasOption('i')) ? cmd.getOptionValue('i') : "spec.yaml";
            String output = (cmd.hasOption('o')) ? cmd.getOptionValue('o') : "api.pdf";
            GroupBy group = (cmd.hasOption('g')) ? GroupBy.TAGS : GroupBy.AS_IS;
            boolean generateExamples = (!cmd.hasOption('e'));

            Path inputFile = Paths.get(input);
            File outputFile = new File(output);
            File templateDirectory = new File(styleDir);

            Swagger2MarkupConfigBuilder configBuilder = new Swagger2MarkupConfigBuilder()
                    .withMarkupLanguage(MarkupLanguage.ASCIIDOC)
                    .withOutputLanguage(Language.EN)
                    .withPathsGroupedBy(group)
                    .withInterDocumentCrossReferences();

            if (generateExamples) configBuilder.withGeneratedExamples();

            Swagger2MarkupConfig config = configBuilder.build();

            Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(inputFile)
                    .withConfig(config)
                    .build();

            //converter.toFile(tempFile);
            String adoc = converter.toString();

            // String to append to title, used for pdf conversion parameters
            String additional = "\n";


            // add PDF options to additional string
            if (cmd.hasOption('t')) additional += ":toc:\n";
            if (cmd.hasOption('d')) additional += String.format(":pdf-stylesdir: %s\n", styleDir);
            if (cmd.hasOption('f')) additional += String.format(":pdf-fontsdir: %s\n", fontsDir);
            if (cmd.hasOption('s')) additional += String.format(":pdf-style: %s\n", style);
            if (cmd.hasOption('p')) additional += String.format(":imagesdir: %s\n", imagesDir);


            Pattern headerPattern = Pattern.compile("^= (.*)$", Pattern.MULTILINE);
            Matcher m = headerPattern.matcher(adoc);

            // RegEx does not properly match document title
            // @see https://github.com/cascer1/swagger-docgen/issues/4
            if (m.matches()) {
                String replacement = String.format("%s%s", m.group(0), additional);

                adoc = m.replaceFirst(replacement);
            }


            System.out.println("m.matches() = " + m.matches());
            System.out.println("m.pattern() = " + m.pattern());
            System.out.println("m = " + m);
            System.out.println("adoc.substring(0,50) = " + adoc.substring(0, 50));

            Asciidoctor asciidoctor = create();

            OptionsBuilder asciidocOptions = options();

            asciidocOptions.templateDir(templateDirectory);
            asciidocOptions.backend("pdf");
            asciidocOptions.docType("book");
            asciidocOptions.safe(SafeMode.SAFE);
            asciidocOptions.toFile(outputFile);

            asciidoctor.convert(adoc, asciidocOptions.get());

        } catch (
                Exception ex)

        {
            ex.printStackTrace(System.err);
        }

    }
}
