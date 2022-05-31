package com.dktechhub.shareit.filetransferapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import android.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.dktechhub.shareit.filetransferapp.R;
import com.dktechhub.shareit.filetransferapp.SharedItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{
    public ArrayList<SharedItem> items = new ArrayList<>();
    public HashMap<String,Integer> map = new HashMap<>();
    Context context;
    boolean sendMode = true;
    public int getItemIndex(String id)
    {
        if(map.containsKey(id))
            return map.get(id);
        else return -1;
    }



    void addMap(HashMap<String,Integer> map)
    {
        this.map.putAll(map);
    }

    public RecyclerViewAdapter(boolean sendMode)
    {
        this.sendMode=sendMode;
    }
    private static final int OUTGOING = 0;
    private static final int INCOMING = 1;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        LayoutInflater inflator = LayoutInflater.from(context);
        View itemView;

        switch (viewType)
        {
            case OUTGOING:
                itemView = inflator.inflate(R.layout.layout_item_send,parent,false);
                return new OutGoingViewHolder(itemView);

            case INCOMING:
                itemView = inflator.inflate(R.layout.layout_item_receive,parent,false);
                return new IncomingViewHolder(itemView);

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String s ="";
        switch (items.get(position).shareState)
        {
            case FAILED:
                s="Retry";break;
            case PENDING:
                s="Cancel";break;
            case COMPLETED:
                s="Open";break;
            case CANCELLED:
                s="Retry";
        }
            if(items.get(position).outGoing)
            {
                OutGoingViewHolder holder1 = (OutGoingViewHolder) holder;
                holder1.name.setText(items.get(position).name);
                holder1.pbar.setProgress(items.get(position).progress);
                holder1.status.setText(items.get(position).shareState.toString());
                holder1.action.setText(s);
                holder1.action.setTag(position);
                holder1.action.setOnClickListener(this);


            }else {
                ((IncomingViewHolder)holder).name.setText(items.get(position).name);
                ((IncomingViewHolder)holder).pbar.setProgress(items.get(position).progress);
                ((IncomingViewHolder)holder).status.setText(items.get(position).shareState.toString());
                ((IncomingViewHolder)holder).action.setText(s);
                ((IncomingViewHolder)holder).action.setTag(position);
                ((IncomingViewHolder)holder).action.setOnClickListener(this);

            }
    }

    @Override
    public int getItemViewType(int position) {

        if(items.get(position).outGoing)
        {
            return OUTGOING;
        }
        else return INCOMING;

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onClick(View v) {
        int p = (int) v.getTag();
        SharedItem item = items.get(p);
        switch (item.shareState)
        {
            case PENDING:
                item.shareState=ShareState.CANCELLED;break;
            case FAILED:
            case CANCELLED:
                item.shareState=ShareState.PENDING;
                if(senderAppCall!=null)
                {
                    Queue<Pair<SharedItem,Integer>> l = new LinkedList<>();
                    l.add(new Pair<>(item,p));
                    senderAppCall.onNewTasksAdded(l);
                }break;
            case COMPLETED:
            {
                Intent i= new Intent();
                i.setDataAndType(item.uri,item.type);
                Log.d("Adapter clicked",item.name+ item.type);
                i.setAction(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(i);
            }break;


        }
        notifyItemChanged(p);
    }

    public static class IncomingViewHolder extends RecyclerView.ViewHolder{
        public TextView name,size,action,status;
        public ProgressBar pbar;
        public IncomingViewHolder(@NonNull View itemView) {
            super(itemView);
            pbar= itemView.findViewById(R.id.progressBar2);
            name = itemView.findViewById(R.id.file_name);
            size= itemView.findViewById(R.id.size);
            status=itemView.findViewById(R.id.status);
            action= itemView.findViewById(R.id.action);
        }
    }

    public static class OutGoingViewHolder extends RecyclerView.ViewHolder{
        public TextView name,size,action,status;
        public ProgressBar pbar;
        public OutGoingViewHolder(@NonNull View itemView) {
            super(itemView);
            pbar= itemView.findViewById(R.id.progressBar2);
            name = itemView.findViewById(R.id.file_name);
            size= itemView.findViewById(R.id.size);
            status=itemView.findViewById(R.id.status);
            action= itemView.findViewById(R.id.action);
        }
    }

    void addItems(ArrayList<SharedItem> list)
    {

        if(list.size()>0){
            items.addAll(list);
            notifyItemRangeInserted(items.size()- list.size(), list.size());
            Queue<Pair<SharedItem,Integer>> queue = new LinkedList<>();
            if(sendMode)
            {
                for(int i=items.size()- list.size();i<items.size();i++)
                {
                    queue.add(new Pair<>(items.get(i),i ));
                }
                if(senderAppCall!=null)
                    senderAppCall.onNewTasksAdded(queue);
            }else {
                for(int i=items.size()- list.size();i<items.size();i++)
                {
                    map.put(items.get(i).id,i);
                }   
            }
            
            
        }

    }
    
    SenderAppCall senderAppCall;
    public void setSenderAppCall(SenderAppCall senderAppCall)
    {
        this.senderAppCall=senderAppCall;
    }
    
    public interface SenderAppCall{
        void onNewTasksAdded(Queue<Pair<SharedItem,Integer>> queue);
    }
}
