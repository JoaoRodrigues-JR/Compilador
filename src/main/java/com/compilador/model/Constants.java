package com.compilador.model;

public interface Constants extends ScannerConstants, ParserConstants
{
    int EPSILON  = 0;
    int DOLLAR   = 1;

    int t_id = 2;
    int t_cte_int = 3;
    int t_cte_float = 4;
    int t_cte_char = 5;
    int t_cte_string = 6;
    int t_TOKEN_7 = 7; //"&"
    int t_TOKEN_8 = 8; //"|"
    int t_TOKEN_9 = 9; //"!"
    int t_TOKEN_10 = 10; //"=="
    int t_TOKEN_11 = 11; //"!="
    int t_TOKEN_12 = 12; //"<"
    int t_TOKEN_13 = 13; //">"
    int t_TOKEN_14 = 14; //"+"
    int t_TOKEN_15 = 15; //"-"
    int t_TOKEN_16 = 16; //"*"
    int t_TOKEN_17 = 17; //"/"
    int t_TOKEN_18 = 18; //","
    int t_TOKEN_19 = 19; //";"
    int t_TOKEN_20 = 20; //"="
    int t_TOKEN_21 = 21; //"("
    int t_TOKEN_22 = 22; //")"
    int t_pr_bool = 23;
    int t_pr_case = 24;
    int t_pr_char = 25;
    int t_pr_echo = 26;
    int t_pr_do = 27;
    int t_pr_end = 28;
    int t_pr_false = 29;
    int t_pr_float = 30;
    int t_pr_int = 31;
    int t_pr_local = 32;
    int t_pr_module = 33;
    int t_pr_request = 34;
    int t_pr_string = 35;
    int t_pr_switch = 36;
    int t_pr_true = 37;
    int t_pr_until = 38;
    int t_pr_while = 39;

}
