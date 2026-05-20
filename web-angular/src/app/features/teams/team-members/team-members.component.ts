import { Component, input } from '@angular/core';
import { Membership } from '../../../core/models/membership.model';

@Component({
  selector: 'app-team-members',
  template: `
    <section class="team-members">
      <div class="header">
        <h3>Members</h3>
        <p>Membership list placeholder ready for backend integration.</p>
      </div>

      @if (members().length === 0) {
        <p class="empty-state">No membership data loaded yet.</p>
      } @else {
        <ul>
          @for (member of members(); track member.id) {
            <li>
              <strong>{{ member.user?.firstName }} {{ member.user?.lastName }}</strong>
              <span>{{ member.role }}</span>
            </li>
          }
        </ul>
      }
    </section>
  `,
  styleUrl: './team-members.component.scss',
})
export class TeamMembersComponent {
  readonly members = input<Membership[]>([]);
}
