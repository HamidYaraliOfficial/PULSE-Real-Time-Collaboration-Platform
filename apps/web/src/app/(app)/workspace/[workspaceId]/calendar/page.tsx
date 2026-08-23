import { CalendarView } from "@/components/calendar/calendar-view";

export default function CalendarPage({ params }: { params: { workspaceId: string } }) {
  return <CalendarView workspaceId={params.workspaceId} />;
}
