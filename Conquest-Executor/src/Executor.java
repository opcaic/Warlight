public class Executor
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
        if (args.length != 4)
        {
            ExitError("Four args expected.");
        }
        try
        {
            ProcessBuilder builder = new ProcessBuilder("java", "-jar", "Conquest-Tournament.jar", args[0], args[1], args[2], args[3]);
            builder.inheritIO();
            Process p = builder.start();
            p.waitFor();

            if (p.exitValue() != 0)
            {
                ExitError("Match was not executed correctly.");
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
