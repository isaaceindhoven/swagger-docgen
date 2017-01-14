package nl.isaac.apidocs.swagger.Factory;

import org.apache.commons.cli.Options;

/**
 * Created by loci on 14-1-17.
 * Creates an {@link org.apache.commons.cli.Options} class with the required options.
 */
public final class OptionFactory {
    private OptionFactory() { throw new AssertionError("This class should not be instantiated."); }

    /**
     * Builds an option class and returns it.
     * @return {@link org.apache.commons.cli.Options}
     */
    public static Options getOptions() {
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

        return options;
    }
}
