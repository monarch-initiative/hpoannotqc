package org.monarchinitiative.hpoannotqc.bigfile;

import java.util.Arrays;
import java.util.stream.Collectors;


/**
 * This is a convenience class where we will put the fields of the BigFile. The class has static methods
 * representing the old and the new Bigfile formats
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class BigFileHeader {

/*
DB 	Yes |MIM, ORPHA, DECIPHER
2 	DB_Object_ID 	Yes |154700
3 	DB_Name 	Yes |Achondrogenesis, type IB
4 	Qualifier 	No |NOT
5 	HPO_ID 	Yes |HP:0002487
6 	DB_Reference 	Yes |OMIM:154700 or PMID:15517394
7 	Evidence_Code 	Yes | IEA
8 	Onset_Modifier 	No | HP:0003577
9 	Frequency_Modifier 	No | HP:0003577 of 12/45 or 22%
10 	Sex_Modifier 	No | MALE or FEMALE
11 	Modifier 	No | HP:0003577 (“;”-separated list)
12 	Aspect 	Yes | “O” or “M” or “I” or “C”
13 	Date_Created 	Yes | YYYY-MM-DD
14 	Assigned_By
 */
    /**
     * @return Header line for the new V2 small files.
     */
    public static String getHeaderV2() {
        String []fields={"#DB",
                "DB_Object_ID",
                "DB_Name",
                "Qualifier",
                "HPO_ID",
                "DB_Reference",
                "Evidence_Code",
                "Onset",
                "Frequency",
                "Sex",
                "Modifier",
                "Aspect",
                "Date_Created",
                "Assigned_By"};
        return Arrays.stream(fields).collect(Collectors.joining("\t"));
    }




    /**
     * @return Header line for the V1 big file (the format in use from 2009 to the beginning of 2018)
     */
    public static String getHeaderV1() {
        String []fields={"#DB",
                "DB_Object_ID",
                "DB_Name",
                "Qualifier",
                "HPO ID",
                "DB:Reference",
                "Evidence code",
                "Onset modifier",
                "Frequency modifier",
                "With",
                "Aspect",
                "Synonym",
                "Date",
                "Assigned by"};
        return Arrays.stream(fields).collect(Collectors.joining("\t"));
    }
}
