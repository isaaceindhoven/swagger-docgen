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
import nl.isaac.apidocs.swagger.configbuilder.ConfigurationBuilder;
import nl.isaac.apidocs.swagger.factory.OptionFactory;
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
            Options options = OptionFactory.getOptions();

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(cmd);

            // Load Swagger spec
            String styleDir = (cmd.hasOption('d')) ? cmd.getOptionValue('d') : "styles";
            String input = (cmd.hasOption('i')) ? cmd.getOptionValue('i') : "spec.yaml";
            String output = (cmd.hasOption('o')) ? cmd.getOptionValue('o') : "api.pdf";

            Path inputFile = Paths.get(input);
            File outputFile = new File(output);
            File templateDirectory = new File(styleDir);

            // Initialize Swagger2Markup config
            Swagger2MarkupConfigBuilder configBuilder = configurationBuilder.getSwaggerBuilder();

            if (cmd.hasOption('r')) configBuilder.withHeaderRegex(cmd.getOptionValue('r'));

            Swagger2MarkupConfig config = configBuilder.build();

            // Convert Swagger spec to asciidoc
            Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(inputFile)
                    .withConfig(config)
                    .build();

            // Save conversion result to String
            String adoc = converter.toString();

            // String to append to title, used for pdf conversion parameters
            String additional = configurationBuilder.getAdditionalPDFOptions();

            // Find document title and append PDF options
            Pattern headerPattern = Pattern.compile("^= (.*)", Pattern.MULTILINE);
            Matcher m = headerPattern.matcher(adoc);

            if (m.find()) {
                String replacement = String.format("%s%s", m.group(0), additional);
                adoc = m.replaceFirst(replacement);
            }

            // Create asciidoctor-j instance for conversion to PDF
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
