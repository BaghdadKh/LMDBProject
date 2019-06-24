import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect
import java.nio.charset.StandardCharsets.UTF_8

import org.lmdbjava.DbiFlags.MDB_CREATE
import org.lmdbjava.Env
import org.lmdbjava.Env.{Builder, create}
import org.lmdbjava.EnvFlags.MDB_NOSUBDIR
import scalaz.zio._
import scalaz.zio.console._
// Create LMDB and use ZIO to read value from
object ZIOReadLMDB extends App {
  //Setting env
  val DB_NAME = "my DB2"
  val file = new File("testZIO.txt")
  val createEnv : Builder[ByteBuffer] = create().setMapSize(10485760).setMaxDbs(1)
  val env = createEnv.open(file,MDB_NOSUBDIR)
  val db = env.openDbi(DB_NAME, MDB_CREATE)
  //define key value record
  val key = allocateDirect(env.getMaxKeySize)
  val value = allocateDirect(700)
  //insert to db
  key.put("Greeting".getBytes(UTF_8)).flip
  value.put("Hello World".getBytes(UTF_8)).flip
  db.put(key, value)

  val txn = env.txnRead
  val found : ByteBuffer = db.get(txn,key)
  val fetchedVal : ByteBuffer = txn.`val`()
  txn.commit()
  //fetching data using zio
  override def run(args: List[String]) =
    for {
      readValue <- IO.succeed(fetchedVal)
      _ <-putStrLn(UTF_8.decode(readValue).toString)

    } yield (0)

}