package uk.bowdlerize.support;

public class CensorPayload
{
    public static int CONFIDENCE_BLOCKED_STRING = 100;
    public static int CONFIDENCE_FORBIDDEN_STRING = 100;
    public static int CONFIDENCE_SUSPICIOUS_TIMEOUT = 90;
    public static int CONFIDENCE_SUSPICIOUS_NO_RESPONSE = 80;
    public static int CONFIDENCE_SUSPICIOUS_403 = 50;
    public static int CONFIDENCE_NONE = 0;

    private boolean isCensored = false;
    private int confidence = 0;
    private String confidenceReason = "None";
    private int returnCode = 200;

    public CensorPayload(boolean _censored, int _confidence)
    {
        isCensored = _censored;
        confidence = _confidence;
    }

    public CensorPayload(boolean _censored, int _confidence, String _reason,int response)
    {
        isCensored = _censored;
        confidence = _confidence;
        confidenceReason = _reason;
        returnCode = response;
    }

    public boolean isCensored()
    {
        return isCensored;
    }

    public boolean wasCensored()
    {
        return isCensored;
    }

    public String getConfidenceReason()
    {
        return confidenceReason;
    }

    public int getReturnCode()
    {
        return returnCode;
    }
}
