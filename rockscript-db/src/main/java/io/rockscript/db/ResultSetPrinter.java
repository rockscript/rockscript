/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.rockscript.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetPrinter {

  class TableData {
    List<Column> columns = null;
    List<List<String[]>> data = new ArrayList<>();
    public void initializeColumns(ResultSetMetaData rowMetaData) throws SQLException {
      int columnCount = rowMetaData.getColumnCount();
      columns = new ArrayList<>(columnCount);
      for (int columnIndex=1; columnIndex<columnCount+1; columnIndex++) {
        columns.add(createColumn(rowMetaData, columnIndex));
      }
    }
  }

  abstract class Column {
    int columnIndex;
    String name;
    int maxLength;
    public Column(ResultSetMetaData rowMetaData, int columnIndex) throws SQLException {
      this.columnIndex = columnIndex;
      this.name = rowMetaData.getColumnName(columnIndex);
      this.maxLength = this.name.length();
    }
    abstract String getString(ResultSet resultSet, int columnIndex) throws SQLException;
  }

  class StringColumn extends Column {
    public StringColumn(ResultSetMetaData rowMetaData, int columnIndex) throws SQLException {
      super(rowMetaData, columnIndex);
    }
    @Override
    String getString(ResultSet resultSet, int columnIndex) throws SQLException {
      return resultSet.getString(columnIndex);
    }
  }

  Column createColumn(ResultSetMetaData rowMetaData, int columnIndex) throws SQLException {
    return new StringColumn(rowMetaData, columnIndex);
  }

  public String toString(ResultSet resultSet) {
    TableData tableData = new TableData();
    try {
      while (resultSet.next()) {
        if (tableData.columns==null) {
          ResultSetMetaData rowMetaData = resultSet.getMetaData();
          tableData.initializeColumns(rowMetaData);
        }
        List<String[]> rowData = new ArrayList<>();
        for (Column column: tableData.columns) {
          String cellValue = column.getString(resultSet, column.columnIndex);
          if (cellValue!=null) {
            String[] cellLines = cellValue.split("\n");
            // IDEA: add support for line wrapping
            for (String cellLine: cellLines) {
              if (column.maxLength < cellLine.length()) {
                column.maxLength = cellLine.length();
              }
            }
            rowData.add(cellLines);
          } else {
            rowData.add(null);
          }
        }
        tableData.data.add(rowData);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    StringBuilder out = new StringBuilder();
    if (tableData.columns!=null) {
      for (Column column: tableData.columns) {
        out.append("| ");
        out.append(pad(column.name, column.maxLength));
        out.append(" ");
      }
      out.append("|\n");
      for (Column column: tableData.columns) {
        out.append("+-");
        for (int i=0; i<column.maxLength; i++) {
          out.append("-");
        }
        out.append("-");
      }
      out.append("+\n");
      for (List<String[]> rowData: tableData.data) {
        for (int cellLine = 0; cellLine<getMaxCellLines(rowData); cellLine++) {
          for (Column column: tableData.columns) {
            out.append("| ");
            String cellLineText = getCellLineText(column, rowData, cellLine);
            out.append(cellLineText);
            out.append(" ");
          }
          out.append("|\n");
        }
      }
    } else {
      out.append("\n");
    }

    return out.toString();
  }
  private int getMaxCellLines(List<String[]> rowData) {
    int maxCelLines = 0;
    for (String[] cellLineTexts: rowData) {
      if (cellLineTexts!=null
          && maxCelLines<cellLineTexts.length) {
        maxCelLines = cellLineTexts.length;
      }
    }
    return maxCelLines;
  }
  private String getCellLineText(Column column, List<String[]> rowData, int cellLine) {
    String[] cellLines = rowData.get(column.columnIndex-1);
    String cellLineText = "";
    if (cellLines!=null
        && cellLines.length>cellLine) {
      cellLineText = cellLines[cellLine];
    }
    if (column.maxLength==0) {
      return "";
    }
    return pad(cellLineText, column.maxLength);
  }

  private static String pad(String string, int length) {
    if (length==0) {
      return "";
    }
    return String.format("%1$-"+length+"s", string);
  }
}
