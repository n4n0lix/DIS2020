package de.dis;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

public class TransactionBuffer {

  public static class WriteData {
    public int LSN;
    public String Data;
  }

  public TransactionBuffer(int pTransactionId) {
    m_Id = pTransactionId;
    m_Committed = false;
    m_Buffer = new Hashtable<>();
  }

  public int GetId() {
    return m_Id;
  }

  public void SetCommitted(boolean pIsCommitted) {
    m_Committed = pIsCommitted;
  }

  public boolean IsCommitted () {
    return m_Committed;
  }

  public void Write(int pPageId, int pLSN, String pData) {
    var userData = new WriteData();
    userData.LSN = pLSN;
    userData.Data = pData;

    m_Buffer.put(pPageId, userData);
  }

  public Map<Integer, WriteData> getReadOnlyBuffer() {
    return Collections.unmodifiableMap(m_Buffer);
  }

  private int m_Id;
  private boolean m_Committed;
  private Hashtable<Integer, WriteData> m_Buffer;

}
