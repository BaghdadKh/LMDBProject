import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect
import java.nio.charset.StandardCharsets.UTF_8

import org.lmdbjava.DbiFlags.MDB_CREATE
import org.lmdbjava.Env.create
import org.lmdbjava.EnvFlags.MDB_NOSUBDIR
import org.lmdbjava.{Env, KeyRange}
import scalaz.zio._

import scala.collection.JavaConversions._

object ZioScenarios {

  def main(args: Array[String]): Unit = {

    val simpleVar: ZIO[Env[ByteBuffer], Unit, String] = IO.succeedLazy("Hello world")
    val DB_NAME = "my DB WriteTest"
    val file = new File("writeTest.txt")
    val created = create().setMapSize(10485760).setMaxDbs(1)
    val env = created.open(file, MDB_NOSUBDIR)
    val db = env.openDbi(DB_NAME, MDB_CREATE)

    try {
      val txnw = env.txnWrite()
      try {
        db.put(txnw, bb("fact1"), bb(factorial(1)))
        db.put(txnw, bb("fact2"), bb(factorial(2)))
        db.put(txnw, bb("fact3"), bb(factorial(3)))
        txnw.commit()
      } finally if (txnw != null) txnw.close()
    }

    try {
      val txn = env.txnRead()
      try {
        val cursor = db.iterate(txn, KeyRange.all[ByteBuffer]())
        for (kv <- cursor.iterable) {
          val key = kv.key()
          val value = kv.`val`
          println(UTF_8.decode(key) + " " + UTF_8.decode(value).toString)
        }
      } finally if (txn != null) txn.close()
    }
  }
  def factorial(i: BigInt):BigInt= {
    def fact(i: BigInt, accumulator: BigInt): BigInt = {
      if (i <= 1)
        accumulator
      else
        fact(i - 1, i * accumulator)
    }

    fact(i, 1)
  }

  // method to return wrapped factorial to put on LMDB
  def factorial2(i: BigInt): ZIO[Env[ByteBuffer], Unit, BigInt] = {
    def fact(i: BigInt, accumulator: BigInt): BigInt = {
      if (i <= 1)
        accumulator
      else
        fact(i - 1, i * accumulator)
    }

    IO.succeed(fact(i, 1))
  }
  def bb[A](value: A): ByteBuffer = {
    val bb = allocateDirect(100)
    bb.put(value.toString.getBytes(UTF_8)).flip
    bb
  }
}