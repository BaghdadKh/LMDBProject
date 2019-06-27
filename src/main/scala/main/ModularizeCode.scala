package main

import org.lmdbjava.DbiFlags.MDB_CREATE
import org.lmdbjava.EnvFlags.MDB_NOSUBDIR
import scalaz.zio._
import utils.LMDB

object ModularizeCode extends App {
  val myLmdb = new LMDB()

  override def run(args: List[String]) =
    for {
      env <- myLmdb.createEnv()
      env2 <- myLmdb.openEnv(env, "writeTest2.txt", MDB_NOSUBDIR)
      db <- myLmdb.openDb(env2, "my DB WriteTest2", MDB_CREATE)
      txnWrite <- myLmdb.createWriteTx(env2)
      _ <- myLmdb.putOnLmdb(txnWrite, db, myLmdb.createElement("k"), myLmdb.createElement("V"))
      _ <- myLmdb.putOnLmdb(txnWrite, db, myLmdb.createElement("k7"), myLmdb.createElement("V7"))
      _ <- myLmdb.commitToDb(txnWrite)
      txRead <- myLmdb.createReadTx(env2)
      cursor <- myLmdb.readFromDb(txRead, db)
      _ <- myLmdb.printValues(cursor)
      _ <- myLmdb.commitToDb(txRead)
    } yield (0)

}