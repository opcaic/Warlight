import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Compiler
{
    private static String classpath = "Conquest-Bots.jar%1$sConquest.jar";

    public static void DeleteFolder(File folder) {
        if (folder.isDirectory())
        {
            File[] list = folder.listFiles();
            if (list != null)
            {
                for (int i = 0; i < list.length; i++)
                {
                    File tmpF = list[i];
                    if (tmpF.isDirectory())
                    {
                        DeleteFolder(tmpF);
                    }
                    tmpF.delete();
                }
            }
        }
    }

    private static void ExitOk()
    {
        System.out.println("OK");
        System.exit(0);
    }

    private static void ExitError(String message)
    {
        System.out.println(message);
        System.exit(200);
    }

    private static String GetClasspathToLibraries(File directory)
    {
        StringBuilder builder = new StringBuilder();
        for (File f : directory.listFiles())
        {
            System.out.println(f.getName());
            if (f.isFile() && f.getName().endsWith(".jar"))
            {
              builder.append(f.getPath() + System.getProperty("path.separator"));
            }
        }
        return builder.toString();
    }

    private static File FirstOrDefaultJavaFileFromDirectory(File directory)
    {
        for (File f : directory.listFiles())
        {
            if (f.getName().endsWith(".java"))
                return f;
        }
        return null;
    }

    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            ExitError("Three args expected.");
        }
        try
        {
            File librariesDir = new File(args[0]);
            File sourceDir = new File(args[1]);
            File outputDir = new File(args[2]);

            File sourceFile = FirstOrDefaultJavaFileFromDirectory(sourceDir);
            String botName = sourceFile.getName().replace(".java", "");
            String jarClasspath = String.format(classpath, System.getProperty("path.separator"));
            String outputDirPath = outputDir.getPath();

            System.out.println("Compiling classes...");
            ProcessBuilder builder = new ProcessBuilder("javac", "-d", ".","-classpath", jarClasspath, sourceFile.getPath());
            builder.inheritIO();
            builder.redirectErrorStream(true);
            Process p = builder.start();
            p.waitFor();

            if (p.exitValue() != 0)
            {
                ExitError("There was an error while compiling the classes.");
            }

            System.out.println("Building bot jar...");
            builder = new ProcessBuilder("jar", "cvf", botName + ".jar", "conquest");
            builder.inheritIO();
            builder.redirectErrorStream(true);
            p = builder.start();
            p.waitFor();

            if (p.exitValue() != 0)
            {
                ExitError("There was an error while building the bot jar.");
            }

            System.out.println("Moving compiled jar...");
            File botJar = new File(botName + ".jar");
            File targetBotJar = new File( outputDirPath + System.getProperty("file.separator") + botName + ".jar");
            Files.copy(botJar.toPath(), targetBotJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Cleaning up...");
            Files.delete(botJar.toPath());
            DeleteFolder(new File("conquest"));

            ExitOk();
        }
        catch (Exception e)
        {
            // platform error
            System.exit(42);
        }
    }
}
