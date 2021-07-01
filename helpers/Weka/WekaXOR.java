package Weka_2;

import weka.core.Instances;
import weka.core.converters.ArffSaver; //save arff file
import weka.core.converters.ArffLoader; //load arff file
import weka.core.converters.CSVLoader; //load csv file
import weka.core.converters.CSVSaver; //save csv file

import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.MLPRegressor;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.functions.*;
// import weka.classifiers.functions.MLPClassifier;
// import weka.classifiers.functions.Dl4jMlpClassifier;

// import weka.classifiers.functions.activation.Sigmoid;
import weka.classifiers.functions.activation.Softplus;
// import weka.classifiers.functions.*;
import weka.classifiers.functions.loss.SquaredError;


import weka.classifiers.Classifier; //the interface that all the classifer algorithms extend
import weka.classifiers.evaluation.Evaluation; //class to evaluate the algorithm


import java.io.File;
import java.util.Arrays;

public class WekaXOR {
    public static void main(String[] args) throws Exception{
    //convert train file: form CSV to ARFF
    CSVLoader loaderCSV = new CSVLoader();
    loaderCSV.setSource(new File("Processed_cumData.csv"));
    System.out.println("I comment out all the code because there are some errors here");
    Instances train = loaderCSV.getDataSet(); //train data
    // System.out.println(data);
    System.out.println("train: " + train.toSummaryString());

    // //comment the code out because if runnning the code again will cause error (the file xorTrain.arff already exits)
    // // ArffSaver saverArff = new ArffSaver();
    // // saverArff.setInstances(train); 
    // // saverArff.setFile(new File("C:\\Users\\Ta Le Kien\\Desktop\\summer2020\\xorTrain.arff"));
    // // saverArff.writeBatch();
    
    // //convert test file: form ARFF to CSV
    // ArffLoader loaderArff = new ArffLoader();
    // loaderArff.setFile(new File("xorTest.arff"));
    // Instances test = loaderArff.getDataSet(); //test data - replace all the dependent variables with ?
    // System.out.println("test: " + test.toSummaryString());

    // //comment the code out because if runnning the code again will cause error (the file xorTest.csv already exits)
    // // CSVSaver saverCSV = new CSVSaver();
    // // saverCSV.setInstances(test);
    // // saverCSV.setFile(new File("C:\\Users\\Ta Le Kien\\Desktop\\summer2020\\xorTest.csv"));
    // // saverCSV.writeBatch();

    
    train.setClassIndex(train.numAttributes() - 1);
    // test.setClassIndex(test.numAttributes() - 1);
    // set class index to the last column - xor column (class index represents y - the dependent variable) 

    System.out.println("Linear regression result: ");
    classification(train, train, "linear");

    // // System.out.println("Neural network result: ");
    // // classification(train, test, "mlp");

    // System.out.println("MultilayerPerceptron Regressor result: ");
    // classification(train, test, "mlpRegressor");

    }

    public static void classification(Instances train, Instances test, String algo) throws Exception{
        if(algo.equals("linear")){
            SimpleLinearRegression linear = new SimpleLinearRegression();
            Classifier cls = linear;
            cls.buildClassifier(train);

            for(int i = 0; i < test.numInstances(); i++){
                //predict the value of xor
                double clsLabel = linear.classifyInstance(test.instance(i));
                System.out.println("instance: " + test.instance(i));
                //print out the prediction
                System.out.println("prediction: " + clsLabel + " actual: " + train.instance(i).toDoubleArray()[train.numAttributes() - 1]);
                
            }
            System.out.println(linear);

            Evaluation eval = new Evaluation(train);
            eval.evaluateModel(cls, train);
            System.out.println("Linear Regression Evaluation");
            System.out.println(eval.toSummaryString());
            
        }
        if(algo.equals("mlp")){
            MultilayerPerceptron mlp = new MultilayerPerceptron ();
            Classifier cls = mlp;
            cls.buildClassifier(train);
            // mlp.setHiddenLayers("5,5,5"); - 3 hidden layers with 5 units for each layer
            mlp.setHiddenLayers("3"); // one hidden layer with 3 units
            mlp.setLearningRate(0.3);
            mlp.setTrainingTime(100000); //100000 epoches
            
            for(int i = 0; i < test.numInstances(); i++){
                 //predict the value of xor
                double clsLabel = cls.classifyInstance(test.instance(i));
                System.out.println("instance: " + test.instance(i));
                //print out the prediction
                System.out.println("prediction: " + clsLabel + " actual: " + train.instance(i).toDoubleArray()[train.numAttributes() - 1]);
                
            }
            System.out.println("One hidden layer with " + mlp.getHiddenLayers() + " units");
            System.out.println("Num epoch: " + mlp.getTrainingTime());

            Evaluation eval = new Evaluation(train);
            eval.evaluateModel(cls, train);
            System.out.println("Multilayer Perceptron Evaluation: ");
            System.out.println(eval.toSummaryString(true));

            //print out the root mean squared error only
            System.out.println("Root mean square error: " + eval.rootMeanSquaredError());
            
        }


        if(algo.equals("mlpRegressor")){
            MLPRegressor mlpr = new MLPRegressor();
            mlpr.setActivationFunction(new Softplus());
            mlpr.setLossFunction(new SquaredError());
            // mlpr.seth
            String[] optionHiddenUnit = {"-N", "3"}; //3 hidden unit 
            mlpr.setOptions(optionHiddenUnit);
            // mlpr.set
            Classifier cls = mlpr;
            cls.buildClassifier(train);
            for(int i = 0; i < test.numInstances(); i++){
                //predict the value of xor
               double clsLabel = cls.classifyInstance(test.instance(i));
               System.out.println("instance: " + test.instance(i));
               //print out the prediction
               System.out.println("prediction: " + clsLabel + " actual: " + train.instance(i).toDoubleArray()[train.numAttributes() - 1]);
               
           }

        //    System.out.println(mlpr);
            Evaluation eval = new Evaluation(train);
            eval.evaluateModel(cls, train);
            System.out.println("MultilayerPerceptron Regressor Evaluation: ");
            System.out.println(eval.toSummaryString(true));
        }
    }
}