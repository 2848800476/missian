/**
 * 
 */
package com.missian.server.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.statemachine.DecodingState;

/**
 * @ClassName: CheckTransportState
 * @Description: TODO(������һ�仰��������������)
 * @author ��ʤ�(dingshengyu@snda.com)
 * @date 2010-11-29 ����02:19:15
 *
 */
public class CheckTransportState implements DecodingState {

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.codec.statemachine.DecodingState#decode(org.apache.mina.core.buffer.IoBuffer, org.apache.mina.filter.codec.ProtocolDecoderOutput)
	 */
	public DecodingState decode(IoBuffer in, ProtocolDecoderOutput out)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.codec.statemachine.DecodingState#finishDecode(org.apache.mina.filter.codec.ProtocolDecoderOutput)
	 */
	public DecodingState finishDecode(ProtocolDecoderOutput out)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
