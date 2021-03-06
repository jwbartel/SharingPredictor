package snml.usercase;

import java.io.File;

import snml.dataconvert.BasicFeatureExtractor;
import snml.dataconvert.IntermediateDataSet;
import snml.dataconvert.SuperFeatureExtractor;
import snml.dataconvert.weka.WekaDataInitializer;
import snml.dataconvert.weka.WekaDataSet;
import snml.dataimport.ThreadDataSet;
import snml.dataimport.json.JsonDataConfig;
import snml.dataimport.json.JsonThreadParser;
import snml.dataimport.json.yahooanswers.YahooAnswersAnswerConfig;
import snml.dataimport.json.yahooanswers.YahooAnswersDataConfig;
import snml.dataimport.json.yahooanswers.YahooAnswersQuestionConfig;
import snml.rule.basicfeature.IBasicFeatureRule;
import snml.rule.basicfeature.ReadFromFileNumericRule;
import snml.rule.basicfeature.ReadFromFileNumericVectorRule;
import snml.rule.basicfeature.ResponseAvgCharLength;
import snml.rule.basicfeature.ResponseNumRule;
import snml.rule.superfeature.ISuperFeatureRule;
import snml.rule.superfeature.WeightedSumRule;
import snml.rule.superfeature.model.weka.IWekaModelRule;
import snml.rule.superfeature.model.weka.WekaLinearRegressionModelRule;

/**
 * We use a YahooAnswers answer quality correlation case derived from F.M. Harper and his colleagues' 
 * work of analyzing factors affecting answer quality across common Q\&A sites, including Google Answers, 
 * Library Reference, AllExperts, YahooAnswers, and Live QnA \cite{Harper:2008:PAQ:1357054.1357191}. 
 * Here we only use the part of YahooAnswers for case study.
 *
 * @author Jinjing Ma (jinjingm@cs.unc.edu)
 * @version $1$
 */
public class YahooAnswersQualityCorrelation {

	public static void main(String[] args) throws Exception {
		
		/** Define the data config for YahooAnswers question and answer */
		JsonDataConfig qconfig = new YahooAnswersQuestionConfig();
		JsonDataConfig aconfig = new YahooAnswersAnswerConfig();
		
		/** Create a parser to parse YahooAnswers question and answer data
		  * from given file directory 
		  */
		JsonThreadParser parser = new JsonThreadParser(qconfig, aconfig, YahooAnswersDataConfig.DATE_DEFAULT);		
		File dirfile = new File("data/YahooAnswers/rawdata");
		ThreadDataSet threads = parser.parseDirectory(dirfile);
		
		/** Create rules to extract basic features of questions.
		 *  These features are human judger related, so rules are defined to read files
		 */
		IBasicFeatureRule[] basicRulesQ = new IBasicFeatureRule[4];
		basicRulesQ[0] = new ReadFromFileNumericVectorRule("questionType", "data/YahooAnswers/type.txt");
		basicRulesQ[1] = new ReadFromFileNumericVectorRule("gratitude", "data/YahooAnswers/gratitude.txt");
		basicRulesQ[2] = new ReadFromFileNumericVectorRule("priorEffort", "data/YahooAnswers/priorEffort.txt");
		basicRulesQ[3] = new ReadFromFileNumericVectorRule("topic", "data/YahooAnswers/topic.txt");
		
		/** Create intermediate data initializer to define particular
		  * intermediate data initializing in basic feature extracting
		  * Here we use Weka intermediate dataset
		  */
		WekaDataInitializer initializer = new WekaDataInitializer();
		BasicFeatureExtractor basicExtractor = new BasicFeatureExtractor(initializer);	
		
		/** Extract basic features, stored in an IntermediateDataSet */
		IntermediateDataSet featureSet = basicExtractor.extract(threads, "questionFeature", basicRulesQ);
		
		/** Create rules to extract basic measurements of answer quality.
		 */
		IBasicFeatureRule[] basicRulesA = new IBasicFeatureRule[4];
		basicRulesA[0] = new ResponseNumRule("answerNum");
		basicRulesA[1] = new ResponseAvgCharLength("answerAvgL", YahooAnswersDataConfig.CONTENT);
		basicRulesA[2] = new ReadFromFileNumericRule("ratedAnswerQuality", "data/YahooAnswers/ratedAnswerQuality.txt");
		basicRulesA[3] = new ReadFromFileNumericRule("ratedAnswerEffort", "data/YahooAnswers/ratedAnswerEffort.txt");
		
		/** Extract basic measurements, stored in an IntermediateDataSet */
		IntermediateDataSet measurementSet = basicExtractor.extract(threads, "answerMeasure", basicRulesA);

		/** Create a super feature extractor to extract features from intermediate data.
		 *  Use the same Weka intermediate data initializer in super feature extracting
		  */
		SuperFeatureExtractor superExtractor = new SuperFeatureExtractor(initializer);
		
		/** Create rules to extract a combined measurement of answer quality.
		 */
		ISuperFeatureRule[] superRulesAnswer = new ISuperFeatureRule[1];
		double[] weight = {0.2, 0.2, 0.3, 0.3};
		superRulesAnswer[0] = new WeightedSumRule("answerQuality", weight);
		IntermediateDataSet combinedMeasurement = superExtractor.extract(measurementSet, "answerMeasure", superRulesAnswer);
		
		/** Merge features and measurement in the same intermediate data set
		 */
		IntermediateDataSet dataset = featureSet.mergeByAttributes(combinedMeasurement);
		dataset.setTargetIndex();
		
		/** Create the rule containing regression model to fit the correlation
		 */
		ISuperFeatureRule[] finalRule = new ISuperFeatureRule[1];
		finalRule[0] = new WekaLinearRegressionModelRule(null);
		((IWekaModelRule)finalRule[0]).train(dataset, null);
		
		/** Use the model's build-in evaluation to do the analysis
		 */
		((IWekaModelRule)finalRule[0]).evaluate((WekaDataSet)dataset, (WekaDataSet)dataset);
		
	}

}
