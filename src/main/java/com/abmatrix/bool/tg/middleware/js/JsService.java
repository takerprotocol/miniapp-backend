package com.abmatrix.bool.tg.middleware.js;

import cn.hutool.core.lang.Pair;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.abmatrix.bool.tg.common.enuma.PrivateKeyType;
import com.abmatrix.bool.tg.dao.entity.BoolUserPrivateKeyFragmentInfo;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.abmatrix.bool.tg.common.constants.NumberConstants.MPC_FRAGMENT_NUMBER;

/**
 * @author chanson
 */
@Slf4j
@Component
public class JsService {

	@Value("${bool.mpc-js-service.url}")
	private String mpcService;

	public JSONArray generateAddress(String engine) {

		String url = String.format("%s/generate_local?engine=%s", mpcService, engine);

		try (var r = HttpRequest.get(url).timeout(30000).execute()) {
			if (r.isOk()) {
				JSONArray array = JSONArray.parseArray(r.body());
				if (array.size() == MPC_FRAGMENT_NUMBER) {
					return array;
				}
			}
		} catch (Exception e) {
			log.info("Generate mpc Address error,result: [{}]", e.getMessage());
		}
		return null;
	}


	public String getEvmAddress(JSONObject data) {

		String address = "";
		String url = String.format("%s/address_local", mpcService);
		try (
				var response = HttpRequest.post(url)
						.body(new JSONObject().fluentPut("key", data).toString())
						.timeout(30000)
						.execute()
		) {

			if (response.isOk()) {
				address = response.body();
			}
		} catch (Exception ignore) {
			log.info("Get mpc address error, [{}],", url);
		}

		return address;
	}

	public String getTonAddress(JSONObject data) {

		String address = "";
		try {

			String url = String.format("%s/address", mpcService);

			HttpResponse response =
					HttpRequest.post(url)
							.body(new JSONObject()
									.fluentPut("key", data)
									.fluentPut("chain", "ton")
									.toString())
							.execute();

			if (response.isOk()) {
				address = response.body();
			} else {
				log.info("Get address error, [{}],", url);
			}
		} catch (Exception ignore) {
		}

		return address;
	}

	public String signTxData(String message, boolean isTx, String privateKeyFragment1, String privateKeyFragment2) {

		String signature = "";
		try {

			String url = String.format("%s/sign_local", mpcService);

			String body =
					new JSONObject()
							.fluentPut("message", message)
							.fluentPut("isTx", isTx)
							.fluentPut("keys", List.of(
									JSONObject.parseObject(privateKeyFragment1),
									JSONObject.parseObject(privateKeyFragment2))
							)
							.fluentPut("t", 1)
							.toString();

			HttpResponse response =
					HttpRequest.post(url)
							.body(body)
							.execute();

			if (response.isOk()) {
				signature = response.body();
			} else {
				log.info("Sign tx error, [{}],", url);
			}
		} catch (Exception ignore) {
		}

		return signature;
	}



	public Pair<String, BoolUserPrivateKeyFragmentInfo> genEvmAddressInfo(Long userId) {

		PrivateKeyType keyType = PrivateKeyType.EVM;
		List<JSONObject> array = generateAddress(keyType.getEngine()).toJavaList(JSONObject.class);
		String address = getEvmAddress(array.get(0));

		BoolUserPrivateKeyFragmentInfo privateKeyInfoDO = new BoolUserPrivateKeyFragmentInfo(userId, keyType, address,
				array.get(0).toJSONString(), array.get(1).toJSONString(), array.get(2).toJSONString());

		return new Pair<>(address, privateKeyInfoDO);
	}

	public Pair<String, BoolUserPrivateKeyFragmentInfo> genTonAddressInfo(Long userId) {
		PrivateKeyType keyType = PrivateKeyType.TON;

		List<JSONObject> array = generateAddress(keyType.getEngine()).toJavaList(JSONObject.class);

		String address = getTonAddress(array.get(0));
		BoolUserPrivateKeyFragmentInfo privateKeyInfoDO = new BoolUserPrivateKeyFragmentInfo(userId, keyType, address,
				array.get(0).toJSONString(), array.get(1).toJSONString(), array.get(2).toJSONString());

		return new Pair<>(address, privateKeyInfoDO);
	}
}
