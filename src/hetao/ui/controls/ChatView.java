package hetao.ui.controls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hetao.ui.adapter.ChatViewAdapter;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.hetao.ui.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChatView extends ListView {

	public ChatView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub	
	}
	public ChatView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub		
	}
	
	ChatViewAdapter adapter;
	
	int chatType=0;
	
	public void InitChat(String username,int chatType){
		
		
        //去掉分割线
		this.chatType = chatType;
		this.setDivider(null);
		adapter=new ChatViewAdapter(this,username);	
		this.setAdapter(adapter);
		adapter.refreshSelectLast();
	}
	
	public View LoadItemView(boolean from){
		int resid=R.layout.hetao_ui_chat_chatitem_from;
		if(!from){
			resid=R.layout.hetao_ui_chat_chatitem_to;
		}
		View mView = LayoutInflater.from(getContext()).inflate(resid, null, true); 
	    return mView;
	}
	
	//发送消息
	public void SendTextMessage(String content){
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
		//如果是群聊，设置chattype,默认是单聊
		if(chatType==1){
			message.setChatType(ChatType.GroupChat);
		}		
		//设置消息body
		TextMessageBody txtBody = new TextMessageBody(content);
		message.addBody(txtBody);
		adapter.SendMessage(message);
	}
	
	public void SendImageMessage(File content){
		
		
		EMMessage message = EMMessage.createSendMessage(EMMessage.Type.IMAGE);
		//如果是群聊，设置chattype,默认是单聊
		if(chatType==1){
			message.setChatType(ChatType.GroupChat);
		}	
		
		//设置消息body
		ImageMessageBody txtBody = new ImageMessageBody(content);
		message.addBody(txtBody);
		adapter.SendMessage(message);
	}
	
	public void ReciveMessage(){
		
	}

}
