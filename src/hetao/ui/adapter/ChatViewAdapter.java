package hetao.ui.adapter;

import java.util.Date;

import hetao.ui.controls.ChatView;

import com.androidquery.AQuery;
import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.util.DateUtils;

import com.hetao.ui.R;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.Spannable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatViewAdapter extends BaseAdapter {

	private EMConversation conversation;
	private EMMessage[] messages = null;

	private Context context;

	private ChatView chatView;
	
	private String chat_userid;

	public ChatViewAdapter(ChatView chatView, String username) {
		this.chatView = chatView;
		this.conversation = EMChatManager.getInstance().getConversation(
				username);
		this.context = chatView.getContext();
		
		this.chat_userid = username;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return messages == null ? 0 : messages.length;
	}

	@Override
	public EMMessage getItem(int position) {
		// TODO Auto-generated method stub
		if (messages != null && position < messages.length && position>=0) {
			return messages[position];
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 获取消息
		final EMMessage message = getItem(position);
		if (message == null)
			return null;

		// /消息类型
		ChatType chatType = message.getChatType();

		// 消息方向判断
		if (message.direct == EMMessage.Direct.SEND) {
			// 发送消息
			convertView = chatView.LoadItemView(false);
			ShowSendMessage(message, convertView, position);
		} else {
			// 接收消息
			convertView = chatView.LoadItemView(true);
			ShowReciveMessage(message, convertView, position);
		}

		return convertView;
	}

	// 收到消息显示
	public void ShowReciveMessage(EMMessage message, View itemView, int position) {
		// 初始化aq
		AQuery aq = new AQuery(itemView);
		// 显示时间
		// 比较相邻的两条消息，如果时间相差较大就显示时间
		EMMessage prevMessage = getItem(position - 1);
		if (prevMessage != null
				&& DateUtils.isCloseEnough(message.getMsgTime(),
						prevMessage.getMsgTime())) {
			aq.id(R.id.timestamp).gone();
		} else {
			aq.id(R.id.timestamp)
					.visible()
					.text(DateUtils.getTimestampString(new Date(message
							.getMsgTime())));
		}

		// 显示用户头像
		aq.id(R.id.iv_userhead).webImage(message.getFrom());
		// 显示用户名
		aq.id(R.id.tv_userid).text(message.getUserName());
		// 添加消息内容
		View content = getMsgContentView(message);
		((ViewGroup) (aq.id(R.id.tv_chatcontent).getView())).addView(content);

	}

	// 发送消息显示
	public void ShowSendMessage(EMMessage message, View itemView, int position) {
		// 初始化aq
		AQuery aq = new AQuery(itemView);
		// 显示时间
		// 比较相邻的两条消息，如果时间相差较大就显示时间
		EMMessage prevMessage = getItem(position - 1);
		if (prevMessage != null
				&& DateUtils.isCloseEnough(message.getMsgTime(),
						prevMessage.getMsgTime())) {
			aq.id(R.id.timestamp).gone();
		} else {
			aq.id(R.id.timestamp)
					.visible()
					.text(DateUtils.getTimestampString(new Date(message
							.getMsgTime())));
		}

		// 显示用户头像
		aq.id(R.id.iv_userhead).webImage(message.getFrom());
		// 显示用户名
		aq.id(R.id.tv_userid).text(message.getUserName());
		// 添加消息内容
		View content = getMsgContentView(message);
		((ViewGroup) (aq.id(R.id.tv_chatcontent).getView())).addView(content);
	
	    //状态显示
		switch (message.status) {
		case SUCCESS: // 发送成功
			aq.id(R.id.pb_sending).gone();
			aq.id(R.id.msg_status).gone();
			break;
		case FAIL: // 发送失败
			aq.id(R.id.pb_sending).gone();
			aq.id(R.id.msg_status).visible();
			break;
		case INPROGRESS: // 发送中
			aq.id(R.id.pb_sending).visible();
			aq.id(R.id.msg_status).gone();
			break;
		default:
			SendMsgInBackground(message,itemView);
			break;
		}
	}

	public View getMsgContentView(EMMessage message) {
		View contentView=null;
		switch (message.getType()) {
		case TXT:
			TextMessageBody txtBody = (TextMessageBody) message.getBody();
			Spannable span = hetao.ui.utils.SmileUtils.getSmiledText(context, txtBody.getMessage());
		    contentView=new TextView(this.context);	
		    new AQuery(contentView).text(span);
			break;
		case IMAGE:
			ImageMessageBody imgBody=(ImageMessageBody) message.getBody();
			contentView =new ImageView(this.context);
			new AQuery(contentView).webImage(imgBody.getRemoteUrl());
			break;
		default:
			break;		
		}
		return contentView;
	}
	
	public void SendMsgInBackground(EMMessage message,View itemView){
		AQuery aq = new AQuery(itemView);
		//发送中
		aq.id(R.id.pb_sending).visible();
		aq.id(R.id.msg_status).gone();
		
		EMChatManager.getInstance().sendMessage(message, new EMCallBack() {

			@Override
			public void onSuccess() {
				
				UpdateView();
			}

			@Override
			public void onError(int code, String error) {
				
				UpdateView();
			}

			@Override
			public void onProgress(int progress, String status) {
			}

		});
	}
	
	
	public void UpdateView(){
		((Activity)this.context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}
	
	public void SendMessage(EMMessage message){
		//设置接收人
		message.setReceipt(chat_userid);
		//把消息加入到此会话对象中
		conversation.addMessage(message);
		refreshSelectLast();
	}
	
	/**
	 * 刷新页面
	 */
	public void refresh() {
		if (handler.hasMessages(HANDLER_MESSAGE_REFRESH_LIST)) {
			return;
		}
		android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST);
		handler.sendMessage(msg);
	}
	
	/**
	 * 刷新页面, 选择最后一个
	 */
	public void refreshSelectLast() {
		handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
		handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_SELECT_LAST));
	}
	
	/**
	 * 刷新页面, 选择Position
	 */
	public void refreshSeekTo(int position) {
		handler.sendMessage(handler.obtainMessage(HANDLER_MESSAGE_REFRESH_LIST));
		android.os.Message msg = handler.obtainMessage(HANDLER_MESSAGE_SEEK_TO);
		msg.arg1 = position;
		handler.sendMessage(msg);
	}
	
	/*
	 * 数据获取与更新
	 */
	private static final int HANDLER_MESSAGE_REFRESH_LIST = 0;
	private static final int HANDLER_MESSAGE_SELECT_LAST = 1;
	private static final int HANDLER_MESSAGE_SEEK_TO = 2;

	Handler handler = new Handler() {

		private void refreshList() {
			// UI线程不能直接使用conversation.getAllMessages()
			// 否则在UI刷新过程中，如果收到新的消息，会导致并发问题
			messages = (EMMessage[]) conversation.getAllMessages().toArray(
					new EMMessage[conversation.getAllMessages().size()]);
			for (int i = 0; i < messages.length; i++) {
				// getMessage will set message as read status
				conversation.getMessage(i);
			}
			notifyDataSetChanged();
		}

		@Override
		public void handleMessage(android.os.Message message) {
			switch (message.what) {
			case HANDLER_MESSAGE_REFRESH_LIST:
				refreshList();
				break;
			case HANDLER_MESSAGE_SELECT_LAST:
				if (chatView != null) {
					if (messages.length > 0) {
						chatView.setSelection(messages.length - 1);
					}
				}
				break;
			case HANDLER_MESSAGE_SEEK_TO:
				int position = message.arg1;
				if (chatView != null) {
					chatView.setSelection(position);
				}
				break;
			default:
				break;
			}
		}
	};

}
