package com.dianping.cat.report.page.model.spi.internal;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public abstract class BaseHistoricalModelService<T> extends ModelServiceWithCalSupport implements ModelService<T>,
      Initializable {
	private String m_name;

	private boolean m_localMode = true;

	public BaseHistoricalModelService(String name) {
		m_name = name;
	}

	protected abstract T buildModel(ModelRequest request) throws Exception;

	public String getName() {
		return m_name;
	}

	@Override
	public void initialize() throws InitializationException {
		ServerConfigManager manager = lookup(ServerConfigManager.class);
		
		m_localMode = manager.isLocalMode();
	}

	@Override
	public ModelResponse<T> invoke(ModelRequest request) {
		ModelResponse<T> response = new ModelResponse<T>();
		Transaction t = newTransaction("ModelService", getClass().getSimpleName());
		t.addData("thread",Thread.currentThread());

		try {
			T model = buildModel(request);

			response.setModel(model);
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			logError(e);
			t.setStatus(e);
			response.setException(e);
		} finally {
			t.complete();
		}

		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		return request.getPeriod().isHistorical();
	}

	protected boolean isLocalMode() {
		return m_localMode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append(getClass().getSimpleName()).append('[');
		sb.append("name=").append(m_name).append(']');

		return sb.toString();
	}
}
