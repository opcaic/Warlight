package conquest.utils;

import java.io.File;

public class Util {
	public static File file(String path) {
		return new File(path.replace('/', File.separatorChar));
	}
}
