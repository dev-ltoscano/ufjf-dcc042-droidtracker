package com.ltosc.droidtracker.protocol.exception;

/**
 * Created by ltosc on 04/08/2016.
 */
public class LoginServiceException extends Exception
{
    public LoginServiceException()
    {
        super("Não foi possível realizar login no serviço DroidTracker");
    }
}
