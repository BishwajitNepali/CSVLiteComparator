package com.thanglastudio.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.thanglastudio.model.CombinedRows;
import com.thanglastudio.model.Row;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class Comparator {
    private static ArrayList<String[]> missingFromTable1 = new ArrayList<>();
    private static ArrayList<String[]> missingFromTable2 = new ArrayList<>();
    private static ArrayList<String[]> matching = new ArrayList<>();
    private static ArrayList<String[]> notmatching = new ArrayList<>();
    static HashMap<String,List<Row>> dataset1= new HashMap<>();
    static HashMap<String,List<Row>> dataset2= new HashMap<>();
    static HashMap<String,List<Row>> duplicateinTable1= new HashMap<>();
    static HashMap<String,List<Row>> duplicateinTable2= new HashMap<>();
    static HashMap<String,List<Row>> uniqueInTable1= new HashMap<>();
    static HashMap<String,List<Row>> uniqueInTable2= new HashMap<>();
    private static LinkedHashMap<String,String>reportInformation= new LinkedHashMap<>();


    public static void main(String[] args) throws FileNotFoundException, DocumentException {
        String[][] table1 = new String[20000][201];
        String[][] table2 = new String[20000][201];
        List<Row> myList1= new ArrayList<>();
        List<Row> myList2= new ArrayList<>();
        String identifierColumns = "0";
        String comparisonColumns = "3|4";
        File file1 = new File("/Users/admin/Downloads/fa.csv");
        File file2 = new File("/Users/admin/Downloads/fb.csv");
        try {

            System.out.println(file1.exists());
            FileReader fileReader = new FileReader(file1);
            CSVReader csvReader = new CSVReaderBuilder(fileReader)
                    .withSkipLines(1)
                    .build();
            String[] nextRecord;
            List<String[]> lines = new ArrayList<String[]>();

            while ((nextRecord = csvReader.readNext()) != null) {
                lines.add(nextRecord);
            }
            table1 = new String[lines.size()][];
            lines.toArray(table1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileReader fileReader = new FileReader(file2);
            CSVReader csvReader = new CSVReaderBuilder(fileReader)
                    .withSkipLines(1)
                    .build();
            String[] nextRecord;
            List<String[]> lines = new ArrayList<String[]>();

            while ((nextRecord = csvReader.readNext()) != null) {
                lines.add(nextRecord);
            }
            table2 = new String[lines.size()][];
            lines.toArray(table2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(table1.length);
        System.out.println(table2.length);

        for (String[] row : table1) {
            String ids = buildIds(row, identifierColumns);
            Row dataset1= new Row(ids,row);
            myList1.add(dataset1);
        }
        dataset1 =collectDuplicates(myList1);
        for(Map.Entry<String,List<Row>> entry:dataset1.entrySet()){
            if(entry.getValue().size()>1){
                duplicateinTable1.put(entry.getKey(),entry.getValue());
            }
        }

        for (String[] row : table2) {
            String ids = buildIds(row, identifierColumns);
            Row dataset2= new Row(ids,row);
            myList2.add(dataset2);
        }
        dataset2 =collectDuplicates(myList2);
        for(Map.Entry<String,List<Row>> entry:dataset2.entrySet()){
            if(entry.getValue().size()>1){
                duplicateinTable2.put(entry.getKey(),entry.getValue());
            }
        }
        uniqueInTable1.putAll(dataset1);
        uniqueInTable1.keySet().removeAll(duplicateinTable1.keySet());
        uniqueInTable2.putAll(dataset2);
        uniqueInTable2.keySet().removeAll(duplicateinTable2.keySet());

        compareFunction(uniqueInTable1, uniqueInTable2, identifierColumns, comparisonColumns);
        reportInformation.put("Data Set 1",dataset1.values().size()+"");
        reportInformation.put("Dara Set 2",dataset2.values().size()+"");

        reportInformation.put("Missing From table 1  ",missingFromTable1.size()+"");
        reportInformation.put("Missing From Table 2",missingFromTable2.size()+"");

        reportInformation.put("Matching  ",matching.size()+"");
        reportInformation.put("Not Matching  ",notmatching.size()+"");

        reportInformation.put("Duplicate in Table1 ",duplicateinTable1.values().size()+"");
        reportInformation.put("Duplicate in Table2 ",duplicateinTable2.values().size()+"");

        reportInformation.put("Unique in table 1 ",uniqueInTable1.values().size()+"");
        reportInformation.put("Unique in table 2 ",uniqueInTable2.values().size()+"");





        createReport();
        System.out.println("Done");
    }

    private static void createReport () throws FileNotFoundException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("/Users/admin/Downloads/comparisonReport.pdf"));

        document.open();

        PdfPTable table = new PdfPTable(2);
        addTableHeader(table);
        addRows(table);
        //addCustomRows(table);
        document.add(table);
        document.close();

    }

    private static void addRows(PdfPTable table) {

        for (Map.Entry entry:reportInformation.entrySet()){
            table.addCell(entry.getKey().toString());
            table.addCell(entry.getValue().toString());
        }
    }

    private static void addTableHeader(PdfPTable table) {
        Stream.of("Data Result ", "Data Size")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    public static HashMap<String, List<Row>>  collectDuplicates(List<Row> rows) {
        HashMap<String,List<Row>> duplicateMap= new HashMap<>();
        for(Row row:rows){
            if(duplicateMap.containsKey(row.getId()) ){
                List<Row> duplicateRows = duplicateMap.get(row.getId());
                duplicateRows.add(row);
            }
            else{
                ArrayList<Row> newDataList= new ArrayList<>();
                newDataList.add(row);
                duplicateMap.put(row.getId(),newDataList);
            }
        }
        return  duplicateMap;
    }

    private static void compareFunction(HashMap<String, List<Row>> table1, HashMap<String, List<Row>> table2, String identifierColumns, String comparisonColumns) {
        HashMap<String, CombinedRows> combinedTables = new HashMap<>();
        HashMap<String, List<String[]>> duplicateTable1 = new HashMap<>();
        for (Map.Entry<String, List<Row>> entry : table1.entrySet()) {
            combinedTables.put(entry.getKey(), new CombinedRows(entry.getValue().get(0).getRowdata(), null));
        }
        for (Map.Entry<String, List<Row>> entry : table2.entrySet()) {
            if(combinedTables.containsKey(entry.getKey())){
                combinedTables.get(entry.getKey()).setRowFromTable2(entry.getValue().get(0).getRowdata());
            }else{
                combinedTables.put(entry.getKey(), new CombinedRows(null, entry.getValue().get(0).getRowdata()));
            }
        }

        for (CombinedRows combinedRow:combinedTables.values()){
            if(combinedRow.getRowFromTable1()==null){
                missingFromTable1.add(combinedRow.getRowFromTable2());
            }
            else if(combinedRow.getRowFromTable2()==null){
                missingFromTable2.add(combinedRow.getRowFromTable1());
            }else if(Arrays.equals(combinedRow.getRowFromTable1(),combinedRow.getRowFromTable2())){
                matching.add(combinedRow.getRowFromTable1());
            }else{
                notmatching.add(combine(combinedRow.getRowFromTable1(),combinedRow.getRowFromTable2()));
            }

        }
        System.out.println("Done");
    }

    private static String[] combine(String[] rowFromTable1, String[] rowFromTable2) {
        String[] result= new String[rowFromTable1.length];
        for (int i = 0; i <rowFromTable1.length ; i++) {
            if (!rowFromTable1[i].equals(rowFromTable2[i])){
                result[i]=rowFromTable1[i]+"|no match"+rowFromTable2[i];
            }else{
                result[i]=rowFromTable1[i]+"|match"+rowFromTable2[i];
            }
        }
        return result;
    }

    private static String buildIds(String[] row, String identifierColumns) {

        StringBuilder ids = new StringBuilder();
        String[] identifiers = Arrays.stream(identifierColumns.split("|"))
                .map(String::trim)
                .filter(id -> !id.equalsIgnoreCase("|"))
                .toArray(String[]::new);
        for (String id : identifiers) {
            int num = Integer.parseInt(id);
            ids.append(row[num]);
        }

        return ids.toString();
    }


}
