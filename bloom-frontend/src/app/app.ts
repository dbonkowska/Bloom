import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Api } from './bloom-api/api';
import { hello } from './bloom-api/fn/hello-controller/hello';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  private api = inject(Api);
  protected message = signal('');

  ngOnInit() {
    this.api.invoke(hello).then(response => {
      this.message.set(response);
    });
  }
}
