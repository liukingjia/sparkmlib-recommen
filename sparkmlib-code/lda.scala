import org.apache.log4j.{ Level, Logger }
import org.apache.spark.{ SparkConf, SparkContext }
import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.clustering.DistributedLDAModel

object lda {

  def main(args: Array[String]) {
    //0 ����Spark����
    val conf = new SparkConf().setAppName("lda")
    val sc = new SparkContext(conf)
    Logger.getRootLogger.setLevel(Level.WARN)
    
    //1 �������ݣ����ص����ݸ�ʽΪ��documents: RDD[(Long, Vector)]
    // ���У�LongΪ����ID��VectorΪ���·ִʺ�Ĵ�����
    // ���Զ�ȡָ��Ŀ¼�µ����ݣ�ͨ���ִ��Լ����ݸ�ʽ��ת����ת����RDD[(Long, Vector)]����
    val data = sc.textFile("/home/jb-huangmeiling/sample_lda_data.txt")
    val parsedData = data.map(s => Vectors.dense(s.trim.split(' ').map(_.toDouble)))
    // Index documents with unique IDs
    val corpus = parsedData.zipWithIndex.map(_.swap).cache()

    //2 ����ģ�ͣ�����ѵ��������ѵ��ģ��
    val ldaModel = new LDA().
      setK(3).
      setDocConcentration(5).
      setTopicConcentration(5).
      setMaxIterations(20).
      setSeed(0L).
      setCheckpointInterval(10).
      setOptimizer("em").
      run(corpus)

    //3 ģ�������ģ�Ͳ��������������
    println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize + " words):")
    // ����ֲ�
    val topics = ldaModel.topicsMatrix
    for (topic <- Range(0, 3)) {
      print("Topic " + topic + ":")
      for (word <- Range(0, ldaModel.vocabSize)) { print(" " + topics(word, topic)); }
      println()
    }
    
    // ����ֲ�����
    ldaModel.describeTopics(4)
    // �ĵ��ֲ�
    val distLDAModel = ldaModel.asInstanceOf[DistributedLDAModel]
    distLDAModel.topicDistributions.collect    

  }

}

