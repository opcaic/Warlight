import java.io.File;

public class Validator
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

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            ExitError("Two args expected.");
        }
        try
        {
            ProcessBuilder builder = new ProcessBuilder("java", "-jar", "Conquest-Tournament.jar", args[0], args[1], args[1]);
            builder.inheritIO();
            Process p = builder.start();
            p.waitFor();

            if (p.exitValue() != 0)
            {
                ExitError("Submission was not validated correctly.");
            }

            ExitOk();
        }
        catch (Exception e)
        {
            // platform error
            System.exit(42);
        }
    }
}
