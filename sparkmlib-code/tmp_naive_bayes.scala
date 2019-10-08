import org.apache.log4j.{ Level, Logger }
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.mllib.classification.{ NaiveBayes, NaiveBayesModel }
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

object tmp_naive_bayes {

  def main(args: Array[String]) {
    //1 ����Spark����
    val conf = new SparkConf().setAppName("naive_bayes")
    val sc = new SparkContext(conf)
    Logger.getRootLogger.setLevel(Level.WARN)

    //��ȡ��������1
    val data = sc.textFile("/home/jb-huangmeiling/sample_naive_bayes_data.txt")
    val parsedData = data.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }

    //�������ݻ���ѵ���������������
    val splits = parsedData.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0)
    val test = splits(1)

    //�½���Ҷ˹����ģ��ģ�ͣ���ѵ��
    val model = NaiveBayes.train(training, lambda = 1.0, modelType = "multinomial")

    //�Բ����������в���
    val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))
    val print_predict = predictionAndLabel.take(20)
    println("prediction" + "\t" + "label")
    for (i <- 0 to print_predict.length - 1) {
      println(print_predict(i)._1 + "\t" + print_predict(i)._2)
    }
    
    val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()

    //����ģ��
    val ModelPath = "/user/huangmeiling/naive_bayes_model"
    model.save(sc, ModelPath)
    val sameModel = NaiveBayesModel.load(sc, ModelPath)

  }
}
