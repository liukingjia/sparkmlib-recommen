
import org.apache.log4j.{ Level, Logger }
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.mllib.classification.{ SVMModel, SVMWithSGD }
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.util.MLUtils

object svm {

  def main(args: Array[String]) {
    //1 ����Spark����
    val conf = new SparkConf().setAppName("svm")
    val sc = new SparkContext(conf)
    Logger.getRootLogger.setLevel(Level.WARN)

    // ��ȡ��������1����ʽΪLIBSVM format
    val data = MLUtils.loadLibSVMFile(sc, "hdfs://192.168.180.79:9000/user/huangmeiling/sample_libsvm_data.txt")

    //�������ݻ���ѵ���������������
    val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)

    //�½��߼��ع�ģ�ͣ���ѵ��
    val numIterations = 100
    val model = SVMWithSGD.train(training, numIterations)

    //�Բ����������в���
    val predictionAndLabel = test.map { point =>
      val score = model.predict(point.features)
      (score, point.label)
    }
    val print_predict = predictionAndLabel.take(20)
    println("prediction" + "\t" + "label")
    for (i <- 0 to print_predict.length - 1) {
      println(print_predict(i)._1 + "\t" + print_predict(i)._2)
    }

    // ������
    val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()
    println("Area under ROC = " + accuracy)

    //����ģ��
    val ModelPath = "/user/huangmeiling/svm_model"
    model.save(sc, ModelPath)
    val sameModel = SVMModel.load(sc, ModelPath)

  }

}
