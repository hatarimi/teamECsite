package jp.co.internous.node.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import jp.co.internous.node.model.domain.MstDestination;
import jp.co.internous.node.model.mapper.MstDestinationMapper;
import jp.co.internous.node.model.mapper.TblCartMapper;
import jp.co.internous.node.model.mapper.TblPurchaseHistoryMapper;
import jp.co.internous.node.model.session.LoginSession;

@Controller
@RequestMapping("/node/settlement")
public class SettlementController {

	@Autowired
	TblCartMapper cartMapper;

	@Autowired
	MstDestinationMapper destinationMapper;

	@Autowired
	TblPurchaseHistoryMapper purchaseHistoryMapper;

	@Autowired
	LoginSession loginSession;

	private Gson gson = new Gson();

	@RequestMapping("/")
	public String index(Model m) {
		long userId = loginSession.getUserId();

		List<MstDestination> destinations = destinationMapper.findByUserId(userId);
		m.addAttribute("loginSession", loginSession);
		m.addAttribute("destinations", destinations);
		return "settlement";
	}

	// 決済処理。
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/complete")
	@ResponseBody
//①DBの購入履歴情報テーブルに商品ごとの決済情報を登録する。
	public boolean complete(@RequestBody String destinationId) {

		Map<String, String> map = gson.fromJson(destinationId, Map.class);
		String id = map.get("destinationId");

		long userId = loginSession.getUserId();
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("destinationId", id);
		parameter.put("userId", userId);
		long insertCount = purchaseHistoryMapper.insert(parameter);

//②登録成功した場合はユーザーに紐づいているDBのカート情報テーブルの情報を削除する。
		long deleteCount = 0;
		if (insertCount > 0) {
			deleteCount = cartMapper.deleteByUserId(userId);
		}
//③削除が成功した場合は商品購入履歴画面に遷移する。
		return insertCount == deleteCount;

	}
}
