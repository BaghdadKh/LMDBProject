package utils

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteBuffer.allocateDirect
import java.nio.charset.StandardCharsets.UTF_8

import org.lmdbjava.{CursorIterator, Dbi, Txn}
import main.ModularizeCode
import org.lmdbjava.Env.{Builder, create}
import org.lmdbjava._
import scalaz.zio.{App, IO, UIO, ZIO}
import scalaz.zio.console.putStrLn

class LMDB_v2 extends App{
  override def run(args: List[String]) = for {
    _ <- putStrLn("")
  }yield 0

  def createEnv(): UIO[Builder[ByteBuffer]] = {
    IO.succeed(create().setMapSize(10485760).setMaxDbs(1))
  }

  def closeTxn(txn:Txn[ByteBuffer]){
    IO.succeed(txn.close())
  }

  def openEnv(env: UIO[Builder[ByteBuffer]], lmdbFile: String, flag: EnvFlags):UIO[Env[ByteBuffer]] =for {
    environment <- env
    openedEnv <- IO.succeed(environment.open(new File(lmdbFile), flag))
  }yield openedEnv

  def openDb(env: UIO[Env[ByteBuffer]], lmdbName: String, flag: DbiFlags) = for{
    environment <- env
    db <- IO.succeed(environment.openDbi(lmdbName, flag))
  } yield db

  def createElement[A](value: A): ByteBuffer = {
    val bb = allocateDirect(200)
    bb.put(value.toString.getBytes(UTF_8)).flip
    bb
  }

  def createReadTx(env: UIO[Env[ByteBuffer]]) = for {
    environment <- env
    txnRead <- IO.succeed(environment.txnRead())
  }yield txnRead

  def createWriteTx(env: UIO[Env[ByteBuffer]]) = for{
    environment <- env
    txnWrite <- IO.succeed(environment.txnWrite())
  } yield txnWrite

  def putOnLmdb(tx: UIO[Txn[ByteBuffer]], db: UIO[Dbi[ByteBuffer]], key: ByteBuffer, value: ByteBuffer) = for{
    db2 <- db
    tx2 <- tx
    put <- IO.succeed(db2.put(tx2, key, value))
  }yield put

  def commitToDb(tx: UIO[Txn[ByteBuffer]]) = for{
    txn <- tx
    commit <- IO.succeed(txn.commit())
  } yield commit

  def readFromDb(txn: UIO[Txn[ByteBuffer]], dbi: UIO[Dbi[ByteBuffer]]):UIO[CursorIterator[ByteBuffer]]=for {
    tx <- txn
    db <- dbi
    cursor <- IO.succeed(db.iterate(tx, KeyRange.all[ByteBuffer]()))
    cur <- IO.succeed(cursor)
  } yield cur

  def getCursor (cur : UIO[CursorIterator[ByteBuffer]]):CursorIterator[ByteBuffer]  ={
    unsafeRun(cur)
  }

  def hasNextCur(c: CursorIterator[ByteBuffer]) = for {
    has <- IO.succeed(c.hasNext)
  } yield (IO.succeed(has))

  def printValues(c: CursorIterator[ByteBuffer]): ZIO[ModularizeCode.Environment, Nothing, CursorIterator[ByteBuffer]] = for {
    flag <- hasNextCur(c)
    hasNext <- flag
    loop <- if (hasNext) {
      val kv = c.next();
      putStrLn(UTF_8.decode(kv.key()).toString + "  " + UTF_8.decode(kv.`val`()).toString).const(true)
    }
    else putStrLn("").const(false)
    _ <- if (loop) printValues(c) else IO.succeed("")
  } yield c
}
