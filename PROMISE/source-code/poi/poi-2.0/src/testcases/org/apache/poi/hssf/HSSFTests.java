package org.apache.poi.hssf;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.hssf.model.TestFormulaParser;
import org.apache.poi.hssf.record.TestAreaFormatRecord;
import org.apache.poi.hssf.record.TestAreaRecord;
import org.apache.poi.hssf.record.TestAxisLineFormatRecord;
import org.apache.poi.hssf.record.TestAxisOptionsRecord;
import org.apache.poi.hssf.record.TestAxisParentRecord;
import org.apache.poi.hssf.record.TestAxisRecord;
import org.apache.poi.hssf.record.TestAxisUsedRecord;
import org.apache.poi.hssf.record.TestBarRecord;
import org.apache.poi.hssf.record.TestBoundSheetRecord;
import org.apache.poi.hssf.record.TestCategorySeriesAxisRecord;
import org.apache.poi.hssf.record.TestChartRecord;
import org.apache.poi.hssf.record.TestDatRecord;
import org.apache.poi.hssf.record.TestDataFormatRecord;
import org.apache.poi.hssf.record.TestDefaultDataLabelTextPropertiesRecord;
import org.apache.poi.hssf.record.TestFontBasisRecord;
import org.apache.poi.hssf.record.TestFontIndexRecord;
import org.apache.poi.hssf.record.TestFormulaRecord;
import org.apache.poi.hssf.record.TestFrameRecord;
import org.apache.poi.hssf.record.TestLegendRecord;
import org.apache.poi.hssf.record.TestLineFormatRecord;
import org.apache.poi.hssf.record.TestLinkedDataRecord;
import org.apache.poi.hssf.record.TestNameRecord;
import org.apache.poi.hssf.record.TestNumberFormatIndexRecord;
import org.apache.poi.hssf.record.TestObjectLinkRecord;
import org.apache.poi.hssf.record.TestPaletteRecord;
import org.apache.poi.hssf.record.TestPlotAreaRecord;
import org.apache.poi.hssf.record.TestPlotGrowthRecord;
import org.apache.poi.hssf.record.TestRecordFactory;
import org.apache.poi.hssf.record.TestSCLRecord;
import org.apache.poi.hssf.record.TestSSTDeserializer;
import org.apache.poi.hssf.record.TestSSTRecord;
import org.apache.poi.hssf.record.TestSSTRecordSizeCalculator;
import org.apache.poi.hssf.record.TestSeriesChartGroupIndexRecord;
import org.apache.poi.hssf.record.TestSeriesIndexRecord;
import org.apache.poi.hssf.record.TestSeriesLabelsRecord;
import org.apache.poi.hssf.record.TestSeriesListRecord;
import org.apache.poi.hssf.record.TestSeriesRecord;
import org.apache.poi.hssf.record.TestSeriesTextRecord;
import org.apache.poi.hssf.record.TestSeriesToChartGroupRecord;
import org.apache.poi.hssf.record.TestSheetPropertiesRecord;
import org.apache.poi.hssf.record.TestStringRecord;
import org.apache.poi.hssf.record.TestSupBookRecord;
import org.apache.poi.hssf.record.TestTextRecord;
import org.apache.poi.hssf.record.TestTickRecord;
import org.apache.poi.hssf.record.TestUnitsRecord;
import org.apache.poi.hssf.record.TestValueRangeRecord;
import org.apache.poi.hssf.record.aggregates.TestRowRecordsAggregate;
import org.apache.poi.hssf.record.aggregates.TestValueRecordsAggregate;
import org.apache.poi.hssf.record.formula.TestFuncPtg;
import org.apache.poi.hssf.usermodel.TestCellStyle;
import org.apache.poi.hssf.usermodel.TestFormulas;
import org.apache.poi.hssf.usermodel.TestHSSFCell;
import org.apache.poi.hssf.usermodel.TestHSSFDateUtil;
import org.apache.poi.hssf.usermodel.TestHSSFPalette;
import org.apache.poi.hssf.usermodel.TestHSSFRow;
import org.apache.poi.hssf.usermodel.TestHSSFSheet;
import org.apache.poi.hssf.usermodel.TestNamedRange;
import org.apache.poi.hssf.usermodel.TestReadWriteChart;
import org.apache.poi.hssf.usermodel.TestWorkbook;
import org.apache.poi.hssf.util.TestAreaReference;
import org.apache.poi.hssf.util.TestCellReference;
import org.apache.poi.hssf.util.TestRKUtil;
import org.apache.poi.hssf.util.TestRangeAddress;
import org.apache.poi.hssf.util.TestSheetReferences;

/**
 * Test Suite for running just HSSF tests.  Mostly
 * this is for my convienience.
 * 
 * @author Andrew C. Oliver acoliver@apache.org
 */
public class HSSFTests
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(HSSFTests.class);
    }

    public static Test suite()
    {
        TestSuite suite =
            new TestSuite("Test for org.apache.poi.hssf.usermodel");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(TestCellStyle.class));
        suite.addTest(new TestSuite(TestFormulas.class));
        suite.addTest(new TestSuite(TestHSSFCell.class));
        suite.addTest(new TestSuite(TestHSSFDateUtil.class));
        suite.addTest(new TestSuite(TestHSSFPalette.class));
        suite.addTest(new TestSuite(TestHSSFRow.class));
        suite.addTest(new TestSuite(TestHSSFSheet.class));
        suite.addTest(new TestSuite(TestNamedRange.class));
        suite.addTest(new TestSuite(TestReadWriteChart.class));
        suite.addTest(new TestSuite(TestWorkbook.class));
        suite.addTest(new TestSuite(TestFormulaParser.class));
        suite.addTest(new TestSuite(TestAreaFormatRecord.class));
        suite.addTest(new TestSuite(TestAreaRecord.class));
        suite.addTest(new TestSuite(TestAxisLineFormatRecord.class));
        suite.addTest(new TestSuite(TestAxisOptionsRecord.class));
        suite.addTest(new TestSuite(TestAxisParentRecord.class));
        suite.addTest(new TestSuite(TestAxisRecord.class));
        suite.addTest(new TestSuite(TestAxisUsedRecord.class));
        suite.addTest(new TestSuite(TestBarRecord.class));
        suite.addTest(new TestSuite(TestBoundSheetRecord.class));
        suite.addTest(new TestSuite(TestCategorySeriesAxisRecord.class));
        suite.addTest(new TestSuite(TestChartRecord.class));
        suite.addTest(new TestSuite(TestDatRecord.class));
        suite.addTest(new TestSuite(TestDataFormatRecord.class));
        suite.addTest(
            new TestSuite(TestDefaultDataLabelTextPropertiesRecord.class));
        suite.addTest(new TestSuite(TestFontBasisRecord.class));
        suite.addTest(new TestSuite(TestFontIndexRecord.class));
        suite.addTest(new TestSuite(TestFormulaRecord.class));
        suite.addTest(new TestSuite(TestFrameRecord.class));
        suite.addTest(new TestSuite(TestLegendRecord.class));
        suite.addTest(new TestSuite(TestLineFormatRecord.class));
        suite.addTest(new TestSuite(TestLinkedDataRecord.class));
        suite.addTest(new TestSuite(TestNumberFormatIndexRecord.class));
        suite.addTest(new TestSuite(TestObjectLinkRecord.class));
        suite.addTest(new TestSuite(TestPaletteRecord.class));
        suite.addTest(new TestSuite(TestPlotAreaRecord.class));
        suite.addTest(new TestSuite(TestPlotGrowthRecord.class));
        suite.addTest(new TestSuite(TestRecordFactory.class));
        suite.addTest(new TestSuite(TestSCLRecord.class));
        suite.addTest(new TestSuite(TestSSTDeserializer.class));
        suite.addTest(new TestSuite(TestSSTRecord.class));
        suite.addTest(new TestSuite(TestSSTRecordSizeCalculator.class));
        suite.addTest(new TestSuite(TestSeriesChartGroupIndexRecord.class));
        suite.addTest(new TestSuite(TestSeriesIndexRecord.class));
        suite.addTest(new TestSuite(TestSeriesLabelsRecord.class));
        suite.addTest(new TestSuite(TestSeriesListRecord.class));
        suite.addTest(new TestSuite(TestSeriesRecord.class));
        suite.addTest(new TestSuite(TestSeriesTextRecord.class));
        suite.addTest(new TestSuite(TestSeriesToChartGroupRecord.class));
        suite.addTest(new TestSuite(TestSheetPropertiesRecord.class));
        suite.addTest(new TestSuite(TestStringRecord.class));
        suite.addTest(new TestSuite(TestSupBookRecord.class));
        suite.addTest(new TestSuite(TestTextRecord.class));
        suite.addTest(new TestSuite(TestTickRecord.class));
        suite.addTest(new TestSuite(TestUnitsRecord.class));
        suite.addTest(new TestSuite(TestValueRangeRecord.class));
        suite.addTest(new TestSuite(TestRowRecordsAggregate.class));
        suite.addTest(new TestSuite(TestAreaReference.class));
        suite.addTest(new TestSuite(TestCellReference.class));
		  suite.addTest(new TestSuite(TestRangeAddress.class));		
        suite.addTest(new TestSuite(TestRKUtil.class));
        suite.addTest(new TestSuite(TestSheetReferences.class));
        
        
        suite.addTest(new TestSuite(TestFuncPtg.class));
		  suite.addTest(new TestSuite(TestValueRecordsAggregate.class));
		  suite.addTest(new TestSuite(TestNameRecord.class));
        
        //$JUnit-END$
        return suite;
    }
}
