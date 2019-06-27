package utils

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

import org.lmdbjava.{CursorIterator, Dbi, Txn}
import scalaz.zio.IO

class Bank(lmdb: LMDB) {
  def createAccount(tx: Txn[ByteBuffer], db: Dbi[ByteBuffer], id: String, amount: Double) = {
    IO.succeed(db.put(tx, lmdb.createElement(id), lmdb.createElement(amount)))
  }

  def withdraw(tx: Txn[ByteBuffer], db: Dbi[ByteBuffer], curs: CursorIterator[ByteBuffer], id: String, amount: Double) = {
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
    IO.succeed(db.put(tx, lmdb.createElement(id), lmdb.createElement(value)))
  }

  def addToBalance(tx: Txn[ByteBuffer], db: Dbi[ByteBuffer], curs: CursorIterator[ByteBuffer], id: String, amount: Double) = {
    var value: Double = 0
    var key: String = ""
    while (curs.hasNext) {
      val kv = curs.next()
      key = UTF_8.decode(kv.key()).toString
      if (key.equals(id)) {
        value = UTF_8.decode(kv.`val`()).toString.toDouble
        value = value + amount
      }
    }
    IO.succeed(db.put(tx, lmdb.createElement(id), lmdb.createElement(value)))
  }
}