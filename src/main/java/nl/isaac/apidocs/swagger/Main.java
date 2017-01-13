/*
 * Copyright (C) 2017  ISAAC
 * Copyright (C) 2017  Cas Eliëns
 *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.isaac.apidocs.swagger;

import io.github.swagger2markup.*;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            options.addOption("h", "highlighter", true, "Source code highlighter. Possible falues: rouge, pygments, coderay");
            options.addOption("r", "headerregex", true, "Regex pattern used for determining operation categories. First capture group will be category name. Cannot be combined with -g");

            options.addOption("g", "groupbytags", false, "Group paths by tags, cannot be combined with -r");
            options.addOption("e", "examples", false, "Generate examples where none are defined");


            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            // Parse arguments or assign default values
            String style = (cmd.hasOption('s')) ? cmd.getOptionValue('s') : "default";
            String styleDir = (cmd.hasOption('d')) ? cmd.getOptionValue('d') : "styles";
            String imagesDir = (cmd.hasOption('p')) ? cmd.getOptionValue('p') : "styles/img";
            String fontsDir = (cmd.hasOption('f')) ? cmd.getOptionValue('f') : "styles/fonts";
            String highlighter = (cmd.hasOption('h')) ? cmd.getOptionValue('h') : "rouge"; // <--TODO: this

            String input = (cmd.hasOption('i')) ? cmd.getOptionValue('i') : "spec.yaml";
            String output = (cmd.hasOption('o')) ? cmd.getOptionValue('o') : "api.pdf";
            GroupBy group = (cmd.hasOption('g')) ? GroupBy.TAGS : GroupBy.AS_IS;
            group = (cmd.hasOption('r')) ? GroupBy.REGEX : group;


            boolean generateExamples = (cmd.hasOption('e'));

            // Page break locations are currently hardcoded
            List<PageBreakLocations> pageBreakLocations = new ArrayList<>(Collections.singletonList(PageBreakLocations.AFTER_OPERATION));

            // Load Swagger spec
            Path inputFile = Paths.get(input);
            File outputFile = new File(output);
            File templateDirectory = new File(styleDir);


            // Initialize Swagger2Markup config
            Swagger2MarkupConfigBuilder configBuilder = new Swagger2MarkupConfigBuilder()
                    .withMarkupLanguage(MarkupLanguage.ASCIIDOC)
                    .withOutputLanguage(Language.EN)
                    .withPathsGroupedBy(group)
                    .withInterDocumentCrossReferences()
                    .withPageBreaks(pageBreakLocations);

            if (generateExamples) configBuilder.withGeneratedExamples();
            if (cmd.hasOption('r')) configBuilder.withHeaderRegex(cmd.getOptionValue('r'));

            Swagger2MarkupConfig config = configBuilder.build();

            // Convert Swagger spec to asciidoc
            Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(inputFile)
                    .withConfig(config)
                    .build();

            // Save conversion result to String
            String adoc = converter.toString();

            // String to append to title, used for pdf conversion parameters
            String additional = "\n";


            // add PDF options to additional string
            // Must be added directly below document title
            if (cmd.hasOption('t')) additional += ":toc:\n";
            additional += String.format(":pdf-stylesdir: %s\n", styleDir);
            additional += String.format(":pdf-fontsdir: %s\n", fontsDir);
            additional += String.format(":pdf-style: %s\n", style);
            additional += String.format(":imagesdir: %s\n", imagesDir);
            additional += String.format(":source-highlighter: %s\n", highlighter);
            if (cmd.hasOption('r')) additional += ":toclevels: 3";

            // Find document title and append PDF options
            Pattern headerPattern = Pattern.compile("^= (.*)", Pattern.MULTILINE);
            Matcher m = headerPattern.matcher(adoc);

            if (m.find()) {
                String replacement = String.format("%s%s", m.group(0), additional);
                adoc = m.replaceFirst(replacement);
            }

            // Create asciidoctor-j instance for convesion to PDF
            Asciidoctor asciidoctor = create();

            OptionsBuilder asciidocOptions = options();

            asciidocOptions.templateDir(templateDirectory);
            asciidocOptions.backend("pdf");
            asciidocOptions.docType("book");
            asciidocOptions.safe(SafeMode.SAFE);
            asciidocOptions.toFile(outputFile);

            // Convert asciidoc String to PDF
            asciidoctor.convert(adoc, asciidocOptions.get());

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }

    }
}
