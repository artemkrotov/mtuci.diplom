package wekademo;

import java.io.*;


import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.FastVector;
import weka.core.Instances;

public class WekaTest {

    public static Evaluation classify(Classifier model,
                                      Instances trainingSet, Instances testingSet) throws Exception {
        Evaluation evaluation = new Evaluation(trainingSet);

        model.buildClassifier(trainingSet);
        evaluation.evaluateModel(model, testingSet);

        return evaluation;
    }



    public static double calculateAccuracy(FastVector predictions) {
        double correct = 0;

        for (int i = 0; i < predictions.size(); i++) {
            NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
            if (np.predicted() == np.actual()) {
                correct++;
            }
        }

        return 100 * correct / predictions.size();
    }

    public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
        Instances[][] split = new Instances[2][numberOfFolds];

        for (int i = 0; i < numberOfFolds; i++) {
            split[0][i] = data.trainCV(numberOfFolds, i);
            split[1][i] = data.testCV(numberOfFolds, i);
        }

        return split;
    }

    public static void main(String[] args) throws Exception {

        new File("1.txt");
        InputStream inputStream = WekaTest.class.getClassLoader()
                .getResourceAsStream("TestStreams.txt");

        BufferedReader datafile = new BufferedReader(new InputStreamReader(inputStream));


        Instances data = new Instances(datafile);
        data.setClassIndex(data.numAttributes() - 1);



        ;



        // Do 10-split cross validation
        Instances[][] split = crossValidationSplit(data, 2);


        // Separate split into training and testing arrays
        Instances[] trainingSplits = split[0];
        Instances[] testingSplits = split[1];

        // Use a set of classifiers
        Classifier[] models = {
                new J48(), // a decision tree
                new PART(),
                new DecisionTable(),//decision table majority classifier
                new DecisionStump(), //one-level decision tree
                new RandomForest()
        };


        //sout train model
        System.out.println("Training split");
        System.out.println("------------------------");
        for (Instances trainingSplit : trainingSplits) {
            System.out.println(trainingSplit.toString());
        }

        System.out.println();
        System.out.println("Testing split");
        System.out.println("------------------------");
        for (Instances testingSplit : testingSplits) {
            System.out.println(testingSplit.toString());
        }
        System.out.println();

        // Run for each model
        for (int j = 0; j < models.length; j++) {

            // Collect every group of predictions for current model in a FastVector
            FastVector predictions = new FastVector();

            // For each training-testing split pair, train and test the classifier
            for (int i = 0; i < trainingSplits.length; i++) {
                Evaluation validation = classify(models[j], trainingSplits[i], testingSplits[i]);


                predictions.appendElements(validation.predictions());

                // Uncomment to see the summary for each training-testing pair.
                //System.out.println(models[j].toString());
            }

            // Calculate overall accuracy of current classifier on all splits
            double accuracy = calculateAccuracy(predictions);

            // Print current classifier's name and accuracy in a complicated,
            // but nice-looking way.
            System.out.println("Accuracy of " + models[j].getClass().getSimpleName() + ": "
                    + String.format("%.2f%%", accuracy)
                    + "\n---------------------------------");
        }

    }
}