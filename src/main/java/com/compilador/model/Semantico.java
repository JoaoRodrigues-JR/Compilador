package com.compilador.model;

public class Semantico implements Constants
{
    public void executeAction(int action, Token token)	throws SemanticError
    {
        System.out.println("A��o #"+action+", Token: "+token);
    }	
}
