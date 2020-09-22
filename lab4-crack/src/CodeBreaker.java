import java.awt.event.ActionEvent;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import client.view.ProgressItem;
import client.view.StatusWindow;
import client.view.WorklistItem;
import network.Sniffer;
import network.SnifferCallback;
import rsa.Factorizer;
import rsa.ProgressTracker;

public class CodeBreaker implements SnifferCallback {

  private final JPanel workList;
  private final JPanel progressList;

  private final JProgressBar mainProgressBar;

  private final ExecutorService pool;

  // -----------------------------------------------------------------------

  private CodeBreaker() {
    StatusWindow w = new StatusWindow();

    workList = w.getWorkList();
    progressList = w.getProgressList();
    mainProgressBar = w.getProgressBar();

    pool = Executors.newFixedThreadPool(2);

  }

  // -----------------------------------------------------------------------

  public static void main(String[] args) {

    /*
     * Most Swing operations (such as creating view elements) must be performed in
     * the Swing EDT (Event Dispatch Thread).
     * 
     * That's what SwingUtilities.invokeLater is for.
     */

    SwingUtilities.invokeLater(() -> {
      CodeBreaker codeBreaker = new CodeBreaker();
      new Sniffer(codeBreaker).start();
    });
  }

  // -----------------------------------------------------------------------

  /** Called by a Sniffer thread when an encrypted message is obtained. */
  @Override
  public void onMessageIntercepted(String message, BigInteger n) {
    // System.out.println("message intercepted (N=" + n + ")...");
    WorklistItem workListItem = new WorklistItem(n, message);
    JButton itemButton = new JButton("Break");
    itemButton.addActionListener((e) -> onBreakButtonClick(workListItem, itemButton, message, n));
    workListItem.add(itemButton);
    workList.add(workListItem);
  }

  private void onBreakButtonClick(WorklistItem workListItem, JButton button, String message, BigInteger n) {
    workList.remove(workListItem);
    ProgressItem progressItem = new ProgressItem(n, message);
    progressList.add(progressItem);

    pool.execute(() -> {
      try {
        ProgressReport progressReport = new ProgressReport(progressItem);
        String decrypted = Factorizer.crack(message, n, (i) -> progressReport.onProgress(i));
        progressReport.onComplete(decrypted);
        // On completion, add a "remove" button
        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener((e) -> onRemoveButtonClick(progressItem));
        progressItem.add(removeButton);

      } catch (InterruptedException e) {
        throw new Error(e);
      }
    });
  }

  private void onRemoveButtonClick(ProgressItem item) {
    progressList.remove(item);
  }

  private class ProgressReport {
    private ProgressItem item;
    private int totalProgress;

    public ProgressReport(ProgressItem item) {
      this.item = item;
      totalProgress = 0;
      mainProgressBar.setMaximum(mainProgressBar.getMaximum() + 1000000);
    }

    public void onProgress(int ppmDelta) {
      totalProgress += ppmDelta;
      item.getProgressBar().setValue(totalProgress);
      mainProgressBar.setValue(mainProgressBar.getValue() + ppmDelta);
      // System.out.println("Progress: " + totalProgress + "/1000000");
    }

    public void onComplete(String decrypted) {
      item.getTextArea().setText(decrypted);
    }

  }
}
