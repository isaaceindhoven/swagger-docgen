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

package nl.isaac.apidocs.swagger.configbuilder;

import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Language;
import io.github.swagger2markup.PageBreakLocations;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import io.github.swagger2markup.markup.builder.MarkupLanguage;
import org.apache.commons.cli.CommandLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by loci on 14-1-17.
 */
public class ConfigurationBuilder {

    private CommandLine cmd;

    public ConfigurationBuilder(CommandLine cmd) {
        this.cmd = cmd;
    }

    public void getSwaggerBuilder() {
        Swagger2MarkupConfigBuilder configBuilder = getSwagger2MarkupConfigBuilder();
    }

    private Swagger2MarkupConfigBuilder getSwagger2MarkupConfigBuilder() {
        GroupBy group = (cmd.hasOption('g')) ? GroupBy.TAGS : GroupBy.AS_IS;
        group = (cmd.hasOption('r')) ? GroupBy.REGEX : group;

        List<PageBreakLocations> pageBreakLocations = new ArrayList<>(Collections.singletonList(PageBreakLocations.AFTER_OPERATION));

        Swagger2MarkupConfigBuilder configBuilder = new Swagger2MarkupConfigBuilder()
                .withMarkupLanguage(MarkupLanguage.ASCIIDOC)
                .withOutputLanguage(Language.EN)
                .withPathsGroupedBy(group)
                .withInterDocumentCrossReferences()
                .withPageBreaks(pageBreakLocations);
        return configBuilder;
    }

    public String getAdditionalPDFOptions() {
        // Parse arguments or assign default values
        String style = (cmd.hasOption('s')) ? cmd.getOptionValue('s') : "default";
        String styleDir = (cmd.hasOption('d')) ? cmd.getOptionValue('d') : "styles";
        String imagesDir = (cmd.hasOption('p')) ? cmd.getOptionValue('p') : "styles/img";
        String fontsDir = (cmd.hasOption('f')) ? cmd.getOptionValue('f') : "styles/fonts";
        String highlighter = (cmd.hasOption('h')) ? cmd.getOptionValue('h') : "rouge"; // <--TODO: this

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
        return additional;
    }
}
