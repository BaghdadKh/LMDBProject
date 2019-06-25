import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

import ModularizeCode.LMDB
import org.lmdbjava.DbiFlags.MDB_CREATE
import org.lmdbjava.EnvFlags.MDB_NOSUBDIR
import org.lmdbjava.{CursorIterator, Dbi, Txn}
import scalaz.zio._

object BankSceanario extends App {

  val lmdb = new LMDB()
  val bank = new Bank()

  override def run(args: List[String]): ZIO[BankSceanario.Environment, Nothing, Int] = for {
    env <- lmdb.createEnv()
    env2 <- lmdb.openEnv(env, "bankTest.txt", MDB_NOSUBDIR)
    db <- lmdb.openDb(env2, "my DB Bank", MDB_CREATE)
    txnWrite <- lmdb.createWriteTx(env2)
    _ <- bank.createAccount(txnWrite, db, "id1", 500)
    _ <- bank.createAccount(txnWrite, db, "id2", 400)
    _ <- bank.createAccount(txnWrite, db, "id3", 600)
    _ <- lmdb.commitToDb(txnWrite)

    txRead <- lmdb.createReadTx(env2)
    cursor <- lmdb.readFromDb(txRead, db)
//        _ <- lmdb.printValues(cursor)

        _ <- lmdb.commitToDb(txRead)
    txnWrite2 <- lmdb.createWriteTx(env2)
    _ <-bank.withdraw(txnWrite,db,cursor,"id2",50.0)
    _<-lmdb.commitToDb(txnWrite2)
    txRead2 <- lmdb.createReadTx(env2)
    cursor2 <- lmdb.readFromDb(txRead2, db)
    _ <- lmdb.printValues(cursor2)

  } yield (0)
}

class Bank {

  def closeTxn(txn:Txn[ByteBuffer]){
    IO.succeed(txn.close())
  }

  def createAccount(tx: Txn[ByteBuffer], db: Dbi[ByteBuffer], id: String, amount: Double) = {
    import BankSceanario.lmdb
    IO.succeed(db.put(tx, lmdb.createElement(id), lmdb.createElement(amount)))
  }

  def withdraw(tx: Txn[ByteBuffer], db: Dbi[ByteBuffer], curs: CursorIterator[ByteBuffer], id: String, amount: Double) = {
    import BankSceanario.lmdb
    var value: Double = 0
    var key: String = ""
    while (curs.hasNext) {
      val kv = curs.next()
      key = UTF_8.decode(kv.key()).toString
      if (key.equals(id)) {
        value = UTF_8.decode(kv.`val`()).toString.toDouble
        if (amount <= value)
          value = value - amount
      }
    }
    IO.succeed(db.put(tx, lmdb.createElement(key), lmdb.createElement(value)))
  }

  def addToBalance(id: String, amount: Double) = {

  }

}