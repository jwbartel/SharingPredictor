package snml.rule.superfeature.model.mahout;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map.Entry;

import org.apache.mahout.cf.taste.common.NoSuchItemException;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorization;

import snml.dataconvert.IntermediateDataSet;
import snml.dataconvert.mahout.MahoutDataSet;

public abstract class MahoutFactorizerModelRule extends
		MahoutCollaborativeFiteringModelRule {

	protected Factorization factorization;

	MahoutDataSet trainingSet;

	public MahoutFactorizerModelRule(String featureName) {
		super(featureName);
	}

	/**
	 * Initializes the factorizer used to create the matrix
	 */
	protected abstract Factorization initializeFactorization(
			MahoutDataSet trainingSet);

	@Override
	public void train(IntermediateDataSet trainingSet, String[] options)
			throws Exception {
		if (trainingSet == null) {
			throw new NullPointerException();
		}
		
		if (!(trainingSet instanceof MahoutDataSet)) {
			throw new Exception("training set must of type "
					+ MahoutDataSet.class.getName());
		}
		this.trainingSet = (MahoutDataSet) trainingSet;
		this.factorization = initializeFactorization(this.trainingSet);
	}

	/**
	 * Returns the predicted preferences as the dot product of item and user
	 * features
	 * @return the dot product
	 */
	private Float dotProduct(double[] userFeatures, double[] itemFeatures) {

		double result = 0;
		for (int i=0; i<userFeatures.length && i<itemFeatures.length; i++) {
			result += userFeatures[i]*itemFeatures[i];
		}
		return (float) result;
	}

	@Override
	public Float estimatePreference(Object user, Object item) {
		
		if (user == null || item == null) {
			return null;
		}

		Long userID = trainingSet.getUserId(user);
		Long itemID = trainingSet.getItemId(item);

		if (userID == null || itemID == null) {
			return null;
		}

		try {
			double[] userFeatures = factorization.getUserFeatures(userID);
			double[] itemFeatures = factorization.getItemFeatures(itemID);
			return dotProduct(userFeatures, itemFeatures);
		} catch (NoSuchUserException | NoSuchItemException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getFactorizationMatricesString() {
		NumberFormat formatter = new DecimalFormat("0.0000E0");
		
		String retVal = "User features\n============\n";
		for (Entry<Long,Integer> entry : factorization.getUserIDMappings()) {
			long userId = entry.getKey();
			try {
				String userLine = "user"+userId+"\t";
				double[] features = factorization.getUserFeatures(userId);
				for (double feature : features) {
					userLine += formatter.format(feature) + " ";
				}
				retVal += userLine + "\n";
			} catch (NoSuchUserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		retVal += "\n";
		
		for (Entry<Long,Integer> entry : factorization.getItemIDMappings()) {
			
			long itemId = entry.getKey();
			try {
				String itemLine = "item"+itemId+"\t";
				double[] features = factorization.getItemFeatures(itemId);
				for (double feature : features) {
					itemLine += formatter.format(feature) + " ";
				}
				retVal += itemLine + "\n";
			} catch (NoSuchItemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retVal;
	}

}
