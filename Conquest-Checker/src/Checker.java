import java.io.File;

public class Checker
{
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
        if (args.length != 2)
        {
            ExitError("Two args expected, name of directory with additional files and source directory of submission.");
        }
        File sourceDirectory = new File(args[1]);
        File sourceFile = FirstOrDefaultJavaFileFromDirectory(sourceDirectory);
        if (sourceFile == null)
            ExitError("No java file found.");
        ExitOk();
    }
}
