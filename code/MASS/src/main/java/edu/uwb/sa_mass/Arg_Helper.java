package edu.uwb.sa_mass;

import java.util.*;
import java.io.*;

public class Arg_Helper implements Serializable{
    
    public Object[] args;

    public Arg_Helper(Object[] _args){
        args = Arrays.copyOf(_args, _args.length);
    }
}