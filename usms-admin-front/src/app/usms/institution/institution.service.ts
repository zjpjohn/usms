import {Injectable} from '@angular/core';
import {Subject} from 'rxjs/Subject';


@Injectable()
export class InstitutionService {

  // 用来产生数据流的数据源
  private messageSource = new Subject<string>();

  // 把数据流转化成 Observable
  message$ = this.messageSource.asObservable();

  sendMessage(msg: string) {
    this.messageSource.next(msg);
  }

}
