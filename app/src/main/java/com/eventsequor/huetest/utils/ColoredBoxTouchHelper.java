package com.eventsequor.huetest.utils;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class ColoredBoxTouchHelper extends ItemTouchHelper.Callback {
  
  private final ColoredBoxTouchListener listener;
  
  public ColoredBoxTouchHelper(ColoredBoxTouchListener listener) {this.listener = listener;}
  
  @Override
  public boolean isLongPressDragEnabled() {
    return false;
  }
  
  @Override
  public boolean isItemViewSwipeEnabled() {
    return false;
  }
  
  @Override
  public int getMovementFlags(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder h) {
    final int dragFlags = ItemTouchHelper.START | ItemTouchHelper.END;
    final int swipeFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
    return makeMovementFlags(dragFlags, swipeFlags);
  }
  
  @Override
  public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder h0, @NonNull RecyclerView.ViewHolder h1) {
    if (h0.getItemViewType() != h1.getItemViewType()) return false;
    return listener.onItemMove(h0.getAdapterPosition(), h1.getAdapterPosition());
  }
  
  @Override
  public void onSwiped(@NonNull RecyclerView.ViewHolder h, int i) {
    // Do nothing
  }
  
  @Override
  public void onSelectedChanged(RecyclerView.ViewHolder h, int actionState) {
    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
      ColoredBoxAdapter.ColoredBoxHolderListener itemViewHolder = (ColoredBoxAdapter.ColoredBoxHolderListener) h;
      itemViewHolder.onItemSelected();
    }
    super.onSelectedChanged(h, actionState);
  }
  
  @Override
  public void clearView(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder h) {
    super.clearView(r, h);
    ColoredBoxAdapter.ColoredBoxHolderListener itemViewHolder = (ColoredBoxAdapter.ColoredBoxHolderListener) h;
    itemViewHolder.onItemClear();
  }
}
