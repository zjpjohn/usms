import {Component, OnInit, Renderer} from '@angular/core';
import {SimpleBaseUtil} from "../../shared/util/simple-base.util";
import {ActivatedRoute, Router} from "@angular/router";
import {HttpService} from "../../core/service/http.service";
import {ConfirmationService, MenuItem, SelectItem} from "primeng/primeng";
import {Privilege} from "./privilege";
import {GlobalVariable} from "../../shared/global-variable";
@Component({
  selector: 'app-privilege',
  templateUrl: './privilege.component.html',
  styleUrls: ['./privilege.component.css']
})
export class PrivilegeComponent extends SimpleBaseUtil<Privilege> implements OnInit {
  selects: SelectItem[];
  selectKey: string;
  key: string;

  constructor(protected router: Router,
              protected route: ActivatedRoute,
              protected httpService: HttpService,
              protected confirmationService: ConfirmationService,
              protected renderer: Renderer) {
    super(router, route, httpService, confirmationService, renderer);
    this.selects = [];
    this.selects.push({label: '请选择查询条件', value: 0});
    this.selects.push({label: '权限名称', value: 1});
    this.selects.push({label: '权限编码', value: 2});
  }

  ngOnInit(): void {
    this.getDataByPage(0, this.page.size, this.filter);
  }

  deleteSelected() {
    let url = GlobalVariable.BASE_URL + 'privilege/delete';
    this.delete(url, 'id');
  }

  getDataByPage(currentPage: any, rowsPerPage: any, filter: any) {
    let url = GlobalVariable.BASE_URL + 'privilege/list';
    this.httpService.findByPage(url, currentPage, rowsPerPage, null).then(
      res => this.setData(res)
    );
  }

  /**
   * 授权操作
   * @param id
   */
  gotoOperAllocation(id: string) {
    this.router.navigate(['operation-allocation', {id: id}], {relativeTo: this.route});
  }

  query() {
    var url = GlobalVariable.BASE_URL + 'privilege/list';
    if (this.selectKey == '1') {
      url = GlobalVariable.BASE_URL + 'privilege/list?label=' + this.key;
    } else if (this.selectKey == '2') {
      url = GlobalVariable.BASE_URL + 'privilege/list?name=' + this.key;
    }
    this.httpService.findByPage(url, 0, this.page.size, null).then(
      res => {
        return this.setData(res);
      }
    );
  }

}
