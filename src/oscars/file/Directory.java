package oscars.file;

import java.io.File;

/** Define the directories to be used - Immutable (though the file system itself can change) */
@SuppressWarnings("serial")
public final class Directory extends File {
    public static final Directory CATEGORIES = new Directory("categories");

    public static final Directory DATA = new Directory("data");

    public static final Directory PLAYERS = new Directory("players");

    private Directory(String inPathname) {
        super(inPathname);
    }
}